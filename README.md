# gyronav
Android gyroscopic control for a map navigation app (Dedicated to OSMAnd project)

# Abstract

All smartphone GPS navigation apps have an obvious for any avid tourist flaw, limiting the use of such apps for 
serious outdoors adventures. The problem is that it requires precise touching of the fragile screen with bare fingers
of one hand, while holding the phone in the other hand in order to control mostly any app in existence. That is hard to do especially if you wear warm or protective gloves, doing that in wet or dirty environment, or simply cannot use both hands for safety reasons. I'm offering an almost ready to implement Android solution which allows to control any software with 4 virtual arrow keys and 2 virtual 
buttons, using one hand only and without any need to touch anything on the phone, as well as making the app aware of the 
phone orientation in 3d space for additional (i.e. power saving / improved handling) automatic functions. The quick proof of concept 
code, utilizing the phone's gyroscope and accelerometer sensors Android API for that, is provided for your consideration.

# Use case implemented

The user wears the smartphone on the neck lanyard or in the front pocket in a tough protective case and occasionally picks it up 
to check the location, verify the recorded track, or add a way point on the digital map. 

The phone screen turns on automatically on pick up, an goes off after a brief pause on returning back to hanging (vertical) position.

User can scroll the map by 1/4 of the screen size by tilting the corresponding side of the phone towards his/her eyes or outwards. E.g. flipping its top sharply away will scroll the map up (you can think of the map as of a fluid you shke to move in the desired direction). I call it X-Y rocking as the gesture implies noticeable change of the screen's angle to your eyes, however the key is the speed (actually the acceleration) of that gesture, not the angle.

If the map's zoom needed, the corresponding Gyro UI function mode can be selected from the special rotary menu by as you've guesed it rotating the phone screen (or you can see it as shaking the top of the phone sideways) in the hand sharply left or right: one time to bring up the rotary menu at the top of the screen, and then repeating that gesture as many times as needed to "slide" the required mode label (i.e. "Zoom") into the center (top) of the menu. The rotary menu disappears after a time-out or on any other Gyro UI Gesture. Now, X-Y rocking the phone forward will zoom out the map for one level, rocking it towards your eyes - zoom it in. Rocking it left or right could be used for fine zooming, but that's not implemented in this example code.

Exactly the same sequence of gestures used to change the map's type and to add way points. Rotate the phone to select the "Map Mode" or "Way Point" item. For the waypoints mode there are 2 commands implemented: rocking down will register the waypoint, assigning it a sequential number, while rocking the screen up will bring another Gyro UI control - the Gyro Keyboard allowing to set the name for the waypoint. The UX is replicating the Garmin eTrex handheld GPS navigator waypoints naming screen with some improvements. Use the X-Y rocking to move the key selector to a desired letter or a command button, and rotate phone left or right to change the focus of the input within the word, or to save the result while the keyboard selector is over the OK button (bottom right). The keyboard is looped over its sides. The Underscore button (bottom left) has a special functionality: it's letter is converted to space on input finish and the excess spaces are trimmed, so the input name must end with the Underscore to allow moving the keyboard selector to the OK button from it on the keyboard in one step (sounds complicated but actually very natural - try it). Also the Underscore button can be used to delete parts of the name at its beginning or end as leading/trailing spaces will be eventually removed.

In order to aid user with the Gyro UI functionality available - the current selection in the rotary menu is displayed at the top of the screen as a minimized label. In the bottom right corner of the screen a round icon, reminding about allowed rocking directions, is displayed as well (that's just by the initial design, in a ral app it should accompany the menu selection at the top, obviously, making a single UI element). Also there are two types of the vibrational feedback used to confirm UI changes, as the visual feedback may not be reliable enough with the phone moving in the hand.

With some practice the gestures are intuitive enough and natural to perform as soon as you have the limits and timeouts tuned to your hands' dexterity and your particular phone/tablet dimensions (use the app menu available under the button in the left bottom corner of the screen to calibrate all the parameters).

# Overview of the code

The provided code, though dirty somewhat, is fully buildable on top of your own Google Maps API key. The minSDK 10 was used to widen the code compatibility. It represents a simple google mapping UI supplemented with some rudimentary navigation options to demonstrate the functionality of all Gyro UI elements. The Gyro UI is built around the mock of the looped carousel menu, allowing to select various essential navigation functions naturally. The main menu of the app provides controls to tune up the Gyro UI algorithms sensitivity to the gestures manually in real time. Some of these controls must be transferred to the app's settings along with the user friendly tuning UI, similar to one provided, as various sizes of Android devices and various hands may need precise tuning of these triggers. The same should be done for the sleep function, as the lanyard attachment point (as well as the preferred pocket orientation) may vary from user to user, and may depend on particular application.

The sensors' controller is coded as a simple events generating service which continues working after the onPause() event. I believe that's a preferred way for making the business end implementation really simple. The service is well incapsulated in the "sensor" sub-module, the rest is obviously app dependent. The sensors' data processing algorithm is very fast, as there are no any usual matrix operations or any complex math utilized, so it can be run on the UI thread.

The power saving trigger is sensing the gravity acceleration to be well along the predefined axis and with a certain direction in order to reduce the number of false positives. The delayed sleep algorithm implementation is still required, as vigorous enough gestures can trigger the selected gravity sensor direction for a moment while operating the Gyro UI. 

The wake-up function is sensing the gravity vector to maximize on the Z axis and being dominant for a while (that's happen when you put the screen horizontally to look at it) and then wake up the screen. So in applications where you may use the screen vertically that behavior must be changed or removed.

The activity's lifecycle events are dealt with only partially, don't rely on them in my code.
