package com.karumi.katasuperheroes;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.karumi.katasuperheroes.di.MainComponent;
import com.karumi.katasuperheroes.di.MainModule;
import com.karumi.katasuperheroes.model.SuperHero;
import com.karumi.katasuperheroes.model.SuperHeroesRepository;
import com.karumi.katasuperheroes.model.error.HeroNotFoundException;
import com.karumi.katasuperheroes.ui.view.SuperHeroDetailActivity;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.List;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.support.test.espresso.Espresso.getIdlingResources;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.unregisterIdlingResources;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.karumi.katasuperheroes.matchers.ToolbarMatcher.onToolbarWithTitle;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SuperHeroDetailActivityTests {

    @Rule
    public DaggerMockRule<MainComponent> daggerRule =
            new DaggerMockRule<>(MainComponent.class, new MainModule()).set(
                    new DaggerMockRule.ComponentSetter<MainComponent>() {
                        @Override
                        public void setComponent(MainComponent component) {
                            SuperHeroesApplication app =
                                    (SuperHeroesApplication) InstrumentationRegistry.getInstrumentation()
                                            .getTargetContext()
                                            .getApplicationContext();
                            app.setComponent(component);
                        }
                    });

    @Rule
    public ActivityTestRule<SuperHeroDetailActivity> activityRule =
            new ActivityTestRule<>(SuperHeroDetailActivity.class, true, false);

    @Mock
    SuperHeroesRepository repository;

    @After
    public void tearDown() {
        List<IdlingResource> idlingResources = getIdlingResources();
        for (IdlingResource resource : idlingResources) {
            unregisterIdlingResources(resource);
        }
    }

    // Test 1
    @Test
    public void showsSuperHeroNameAsTitle() throws Exception {
        SuperHero hero = givenThereIsASuperHero();

        startActivity(hero);

        // Para comprobar si el texto se muestra en el toolbar
        onToolbarWithTitle(hero.getName()).check(matches(isDisplayed()));
    }

    // Test 2
    @Test
    public void showsSuperHeroNameAndDescription() throws Exception {
        SuperHero hero = givenThereIsASuperHero();

        startActivity(hero);

        // Usamos el withId() porque hay dos textos con el mismo valor pero en distintas vistas (toolbar
        // y textview) y para diferenciarlos. Con el allOf() podemos combinar el withId() con el withText()
        onView(allOf(withId(R.id.tv_super_hero_name), withText(hero.getName()))).check(matches(isDisplayed()));
        onView(withText(hero.getDescription())).check(matches(isDisplayed()));
    }

    // Test 3
    @Test
    public void showsAvengerBadgeWithAnAvengerSuperHero() throws Exception {
        SuperHero hero = givenAnAvenger();

        startActivity(hero);

        onView(withId(R.id.iv_avengers_badge)).check(matches(isDisplayed()));
    }

    // Test 4
    @Test
    public void hidesAvengerBadgeWithAnAvengerSuperHero() throws Exception {
        SuperHero hero = givenThereIsASuperHero(false);

        startActivity(hero);

        onView(withId(R.id.iv_avengers_badge)).check(matches(not(isDisplayed())));
    }

    // Test 5
    @Test
    public void hidesErrorTextviewWhenTheHeroeIsFound() throws Exception {
        SuperHero hero = givenThereIsASuperHero();

        startActivity(hero);

        onView(withId(R.id.tv_empty_case)).check(matches(not(isDisplayed())));
    }

    // Test 6
    @Test public void showsSuperHeroDescription() throws HeroNotFoundException {
        SuperHero superHero = givenThereIsASuperHero();

        startActivity(superHero);
        // Simplificar codigo
        scrollToView(R.id.tv_super_hero_description);

        onView(withText(superHero.getDescription())).check(matches(isDisplayed()));
    }

    @Test
    public void showsNotFoundErrorWhenSuperHeroIsNotFound() throws Exception {
        String heroName= givenAnUnexistingHeroID();

        startActivity(heroName);

        onView(withId(R.id.tv_error)).check(matches(isDisplayed()));
    }

    private String givenAnUnexistingHeroID() throws HeroNotFoundException {
        String heroName= "Fatman";
        when(repository.getByName(heroName)).thenThrow(new HeroNotFoundException());

        return heroName;
    }

    private SuperHero givenThereIsASuperHero() throws HeroNotFoundException {
        return givenThereIsASuperHero(false);
    }

    private SuperHero givenAnAvenger() throws HeroNotFoundException {
        return givenThereIsASuperHero(true);
    }

    private SuperHero givenThereIsASuperHero(boolean isAvenger) throws HeroNotFoundException {
        String superHeroName = "SuperHero";
        String superHeroPhoto = "https://i.annihil.us/u/prod/marvel/i/mg/c/60/55b6a28ef24fa.jpg";
        String superHeroDescription = "Super Hero Description";
        SuperHero superHero =
                new SuperHero(superHeroName, superHeroPhoto, isAvenger, superHeroDescription);
        when(repository.getByName(superHeroName)).thenReturn(superHero);

        return superHero;
    }

    private SuperHeroDetailActivity startActivity(SuperHero superHero) {
        return startActivity(superHero.getName());
    }

    private SuperHeroDetailActivity startActivity(String superHeroName) {
        Intent intent = new Intent();
        intent.putExtra("super_hero_name_key", superHeroName);
        return activityRule.launchActivity(intent);
    }

    private void scrollToView(int viewId) {
        onView(withId(viewId)).perform(scrollTo());
    }
}
