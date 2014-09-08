ERNI Moods
==========

Android app
-----------

This is the ERNI Moods app for Android to be developed during the Hack Sessions and the ERNI Hack Day.

####Please use API 19 and build tools 20.0.0
Please check that you have SDK version 19 before working with this project. Please do not change the build version in build.gradle
Thanks... :-)

##Brief architectural overview
The app is built using the Model, View, Controller paradigm, and the classes are packaged in that way.
Additionally:
* the api package creates an abstraction layer that exposes methods to use the API backend
* the service package contains helper classes

##Model
###model.Mood
Represents a Mood object.
###model.User
Represents a User object.

##Controller
###controller.MoodsApp
Class extends android.app.Application and useful for storing globals that will remain regardless of which Activity is currently running.
###controller.EntryPoint
Activity that is the first one created when the application starts. If the user is not registered, the SignUpFragment is inflated. If the user is already registered, then the MyMoodFragment is inflated.
###controller.SignUpFragment
Allows the user to sign up and validates the input.
###controller.MyMoodFragment
The core of the app - here you select your current mood which is then shared with all the other users.
###controller.MoodsNearMeActivity
(not yet implemented)
This shows the moods on a Google Map.
###controller.SettingsActivity
This inflates SettingsFragment which contains the user preferences.

#ToDo List
Here is a list of things that the app would need:


