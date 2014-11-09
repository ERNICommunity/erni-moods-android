ERNI Moods
==========

Android app
-----------

This is the ERNI Moods app for Android, developed during the Hack Sessions and the ERNI Hack Day.
The app is a client for the [Erni Moods backend API] (https://github.com/ERNICommunity/erni-moods-backend)

##Brief architectural overview
* the controller package contains the Android Activity and Fragment classes that drive the frontend
* the model package contains plain old Java classes
* the api package creates an abstraction layer that exposes methods to use the API backend
* the service package contains helper classes

##model
###Mood
Class representing the user's mood
###User
Class representing the user
###GooglePlace
Class representing a mood marker on the map 

##controller
###EntryPoint
Activity that is the first one created when the application starts. We use and swap-in/out Fragments to display different screens
###SignUpFragment
Allows the user to sign up and validates the input.
###LoginFragment
For logging in user who is already signed up.
###MyMoodFragment
The core of the app - here you select your current mood which is then shared with all the other users.
###MoodsNearMeActivity
This shows the moods on a Google Map.
###SettingsActivity
This inflates SettingsFragment which contains the user preferences. The username and password are stored in preferences.xml

##service
###FormValidator
A helper class for validating form input
###MoodsJSONParser
Provides a set of static methods to marshall and unmarshall User and Mood objects to JSON

##api
###UserBackend
Implementation of the Interface IUserBackend to query User data from the backend
###MoodsBackend
Implementation of the Interface IMoodsBackend to query Mood data from the backend
###PlacesBackend
Implementation of the Interface IPlacesBackend to query Mood data from the backend
###InternetAccess
Extends AsyncTask to handle http requests


###How to get involved
Issues are tracked using the github [issues] feature (https://github.com/ERNICommunity/erni-moods-backend/issues)
If you want to get involved, then please get started assigning unresolved issues to yourself and fixing them :-)

