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

package com.karumi.katasuperheroes.usecase;

import android.os.Handler;
import android.os.Looper;
import com.karumi.katasuperheroes.model.SuperHero;
import com.karumi.katasuperheroes.model.SuperHeroesRepository;
import com.karumi.katasuperheroes.model.error.HeroLoadError;
import com.karumi.katasuperheroes.model.error.HeroNotFoundException;

import javax.inject.Inject;

public class GetSuperHeroByName {

  private final SuperHeroesRepository repository;

  @Inject public GetSuperHeroByName(SuperHeroesRepository repository) {
    this.repository = repository;
  }

  public void get(final String name, final Callback callback) {
    new Thread(new Runnable() {
      @Override public void run() {
        loadSuperHeroByName(name, callback);
      }
    }).start();
  }

  private void loadSuperHeroByName(String name, final Callback callback) {
      try {
        final SuperHero superHero = repository.getByName(name);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                callback.onSuperHeroLoaded(superHero);
            }
          });
      } catch (HeroNotFoundException exception) {
          new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                callback.onSuperHeroLoadFailed(HeroLoadError.NOT_FOUND);
            }
          });
      }
  }

  public interface Callback {

    void onSuperHeroLoaded(SuperHero superHero);
    void onSuperHeroLoadFailed(HeroLoadError error);
  }
}
