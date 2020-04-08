# Covid19-Tracker
*A bluetooth proximity application for keeping record of individuals in contact.*


## Following features have been currently implemented

1. User Registration via Firebase Auth Phone authentication service.
   1. User records are stored in Firebase Realtime Database with unique ID Assigned.
   2. Old users upon registeration will be assigned old IDs, and vice versa.

2. Bluetooth discovery of nearby users
   1. Asking users for location and bluetooth permission
   2. Storing nearby devices in Firebase Realtime Database, with the discovery time.
   3. Removed functionality of storing nearby devices in fire base.
   4. Added functionality of searching for devices only using our application.
  
3. SQLite functionality has been added.
   1. Storinng found devices in local database, with their IDs and Logged time.

4. CSV file generation from the local database of devices.

5. Sending user data via email.
   1. Deleting old csv file generated, creating new csv file and attaching it with hard coded email address.
