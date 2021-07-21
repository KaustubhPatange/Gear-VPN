<div align="center">
    <img alt="Icon" src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" width="200" />
</div>

<h2 align="center">
    Gear VPN - Free, Secure & Open sourced VPN
</h2>

<p align="center">
   <img src="https://github.com/KaustubhPatange/Gear-VPN/workflows/build/badge.svg" alt="build"/>
   <a href="https://github.com/KaustubhPatange/Gear-VPN/issues"><img src="https://img.shields.io/github/issues/KaustubhPatange/Gear-VPN.svg"/></a>
</p>

<div align="center">
<a href='https://play.google.com/store/apps/details?id=com.kpstv.vpn'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="200"/></a>
</div>

## Tech Stack

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Google's new UI toolkit for developing native Android apps.
- [Navigator-Compose](https://github.com/KaustubhPatange/navigator/tree/master/navigator-compose) - A better navigation library for Jetpack Compose.
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) - Collection of libraries that help you design robust, testable, and maintainable apps.
  - [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow#stateflow) - To optimally emit states that should consume by multiple consumers.
  - [ViewModel-Compose](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that isn't destroyed on UI changes.
  - [Room](https://developer.android.com/topic/libraries/architecture/room) - SQLite object mapping library.
  - [Workmanager](https://developer.android.com/topic/libraries/architecture/workmanager) - An API that makes it easy to schedule deferrable, asynchronous tasks that are expected to run even if the app exits or the device restarts.
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - Jetpack DataStore is a data storage solution that allows you to store key-value pairs or typed objects with protocol buffers.
- [Dependency Injection](https://developer.android.com/training/dependency-injection) -
  - [Hilt-Dagger](https://dagger.dev/hilt/) - Standard library to incorporate Dagger dependency injection into an Android application.
  - [Hilt-ViewModel](https://developer.android.com/training/dependency-injection/hilt-jetpack) - DI for injecting `ViewModel`.
- [Retrofit](https://square.github.io/retrofit/) - A type-safe HTTP client for Android and Java.
- [Moshi](https://github.com/square/moshi) - A modern JSON library for Kotlin and Java.
- [Lottie](https://github.com/airbnb/lottie-android) - Render beautiful animations straight from After effects.

## Contribute

If you want to contribute to this project, you're always welcome!
See [Contributing Guidelines](CONTRIBUTING.md).

## Trademark

- Design inspiration from [Jakub Kittler](https://dribbble.com/shots/7025990-VPN-Concept).
- Google Play (screenshots) inspiration from [Yahor Urbanovich](https://proandroiddev.com/how-i-made-beautiful-screenshots-for-google-play-developer-experience-61ce108fa6b4).
- Some images & icons (except logo) are from [Icons8](https://icons8.com/).
- Rounded country flags are fetched from [countryflags.com](http://countryflags.com/).

## License

- [The Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)

```
Copyright 2020 Kaustubh Patange

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
