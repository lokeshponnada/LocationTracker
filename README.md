


Design decisions made :
 
	1. Getting location updates : Fused location provider by google play services does the battery optimisation. Since play services may not be installed on all the devices, 		fallback case using Framework Location manager is implemented

	2. Both location and time threshold , location provider  along with sync time have been kept in AppConfig class which can be updated by a config call. This ensures greater 	flexibility to the backend

	3. InOrder to ensure that the app runs irrespective of the Doze mode or the background restrictions introduced in android O, A foreground service is used to keep the app as 	a foreground process

	4. Bounded service is used for one way b/w app and service

	5. Evernote Job Scheduling library is used for posting the data periodically . This is chosen for of its compatibility with all api till Android O

Libraries used :

	1. Retrofit for n/w calls
	2. Evernote Scheduler for job scheduling 
	3. Room for Sqlite handling




Testing Strategy:
	
	1. Stetho tool was used to check the location db entries. It is also used to check the evenote job db

	2. Charles proxy was used to intercept and verify  the network requests 

	3. Fake gps app was used to fake the user location 

	4. Room library used for simple sqlite handling 

	5. adb commands were used to keep the app in doze mode

	// Tested on Android Lollipop and Android Marshmallow physical devices



Things that were not done
    -> When the app is being currently running and the user turns off location, app can stop posting location issuing a notification to the user
    -> Dialog to explain about the permission if user repeatedly denies permission