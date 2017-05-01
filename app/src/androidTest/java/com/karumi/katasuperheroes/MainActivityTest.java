/*
 * Copyright (C) 2015 Karumi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.karumi.katasuperheroes;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import com.karumi.katasuperheroes.di.MainComponent;
import com.karumi.katasuperheroes.di.MainModule;
import com.karumi.katasuperheroes.model.SuperHero;
import com.karumi.katasuperheroes.model.SuperHeroesRepository;
import com.karumi.katasuperheroes.model.error.HeroNotFoundException;
import com.karumi.katasuperheroes.recyclerview.RecyclerViewInteraction;
import com.karumi.katasuperheroes.ui.view.MainActivity;
import com.karumi.katasuperheroes.ui.view.SuperHeroDetailActivity;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.karumi.katasuperheroes.matchers.RecyclerViewItemsCountMatcher.recyclerViewHasItemCount;
import static org.mockito.Mockito.when;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class) @LargeTest
public class MainActivityTest {

   public static final String EMTPY_CASE_STRING = "¯\\_(ツ)_/¯";

   @Rule public DaggerMockRule<MainComponent> daggerRule =
        new DaggerMockRule<>(MainComponent.class, new MainModule()).set(
            new DaggerMockRule.ComponentSetter<MainComponent>() {
                @Override public void setComponent(MainComponent component) {
                SuperHeroesApplication app =
                    (SuperHeroesApplication) InstrumentationRegistry.getInstrumentation()
                        .getTargetContext()
                        .getApplicationContext();
                app.setComponent(component);
              }
              });

   @Rule public IntentsTestRule<MainActivity> activityRule =
        new IntentsTestRule<>(MainActivity.class, true, false);

   @Mock SuperHeroesRepository repository;

    // Test 1
   @Test
   public void showsEmptyCaseIfThereAreNoSuperHeroes() {
        // Given
        givenThereAreNoSuperHeroes();

        // When
        startActivity();

        // Esto significa que si en la vista tenemos algun TextView con el String y el isDisplayed()
        // para asegurarnos de que se esta mostrando
        // Then
        onView(withText(EMTPY_CASE_STRING)).check(matches(isDisplayed()));
    }

    // Test 2
    @Test
    public void showsSuperHeroNameIfThereIsOneSuperHero() throws Exception {
        List<SuperHero> heroesList = givenThereAreSomeSuperHeroes(1);

        startActivity();

        onView(withText(heroesList.get(0).getName())).check(matches(isDisplayed()));
    }

    // Test 3
    @Test
    public void showsManySuperHeroesWhenThereAreManySuperheros() throws Exception {
        List<SuperHero> heroesList = givenThereAreSomeSuperHeroes(10);

        startActivity();

        // Como tenemos que desplazarnos por la lista usamos RecyclerViewInteraction. El withId indica
        // el id del RecyclerView. El matches indica si coincide el textView (withText) con el nombre
        // del superheroe correspondiente
        RecyclerViewInteraction.<SuperHero>onRecyclerView((withId(R.id.recycler_view)))
            .withItems(heroesList)
            .check(new RecyclerViewInteraction.ItemViewAssertion<SuperHero>() {
                @Override
                public void check(SuperHero item, View view, NoMatchingViewException e) {
                    matches(hasDescendant(withText(item.getName()))).check(view, e);
                }
            });
    }

    // Test 4
    @Test
    public void showsAvengersBadge() throws Exception {
        List<SuperHero> heroesList = givenThereAreSomeSuperHeroes(10);

        startActivity();

        // Con el withEffectiveVisibility() podemos saber si un elemento de la UI esta visible o no
        // Para verificar si una vista tiene cualquier estado (INVISIBLE, VISIBLE y GONE) usamos
        // withEffectiveVisibility(). Para el caso de si solo es VISIBLE usar isDisplayed()
        RecyclerViewInteraction.<SuperHero>onRecyclerView((withId(R.id.recycler_view)))
            .withItems(heroesList)
            .check(new RecyclerViewInteraction.ItemViewAssertion<SuperHero>() {
                @Override
                public void check(SuperHero item, View view, NoMatchingViewException e) {
                    matches(hasDescendant(allOf(withId(R.id.iv_avengers_badge), withEffectiveVisibility(
                        item.isAvenger() ?
                        ViewMatchers.Visibility.VISIBLE : ViewMatchers.Visibility.GONE)))).check(view,e);
                }
            });
    }

    // Test 5
    @Test
    public void doesNotShowProgressBarIfThereAreSomeSuperHeroes() throws Exception {
        givenThereAreSomeSuperHeroes(10);

        startActivity();

        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
    }

    // Test 6
    @Test
    public void showsTheCorrectNumberOfSuperHeroes() throws Exception {
        givenThereAreSomeSuperHeroes(15);

        startActivity();


        onView(withId(R.id.recycler_view)).check(
                matches(recyclerViewHasItemCount(15)));
    }

    // Test 7
    @Test
    public void doesntShowEmptyCaseIfThereAreSuperHeroes() throws Exception {
        givenThereAreSomeSuperHeroes(10);

        startActivity();

        onView(withText(EMTPY_CASE_STRING)).check(matches(not(isDisplayed())));
    }

    // Test 8
    @Test
    public void openASuperHeroDetailWhenARowIsTapped() throws Exception {
        List<SuperHero> heroesList = givenThereAreSomeSuperHeroes(10);

        startActivity();

        int superHeroIndex = 0;

        // Simulamos el click sobre un elemento de la lista
        onView(withId(R.id.recycler_view)).
                perform(RecyclerViewActions.actionOnItemAtPosition(superHeroIndex, click()));

        SuperHero selectedSuperHero = heroesList.get(0);
        // La clase a la que vamos tras el click
        intended(hasComponent(SuperHeroDetailActivity.class.getCanonicalName()));
        // El argumento que pasamos
        intended(hasExtra("super_hero_name_key", selectedSuperHero.getName()));
    }

    private void givenThereAreNoSuperHeroes() {
          // Esto significa que cuando llamemos al metodo getAll() se nos devuelva siempre una lista vacia
          when(repository.getAll()).thenReturn(Collections.<SuperHero>emptyList());
    }

    private List<SuperHero> givenThereAreSomeSuperHeroes(int number) throws HeroNotFoundException {
        List<SuperHero> superHeroList = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            SuperHero hero = new SuperHero("Hero " + i, "http://www.photo.com/" + i, i % 2 == 0, "Hero " + i + " description");
            superHeroList.add(hero);
            when(repository.getByName(hero.getName())).thenReturn(hero);
        }

        when(repository.getAll()).thenReturn(superHeroList);

        return superHeroList;
    }

    private MainActivity startActivity() {
    return activityRule.launchActivity(null);
  }
}