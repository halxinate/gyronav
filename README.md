# gyronav
Android gyroscopic control for a map navigation app (Dedicated to OSMAnd project)

# Abstract

All smartphone GPS navigation apps have an obvious for any avid tourist flaw, limiting the use of such apps for 
serious outdoors adventures. The problem is that it requires precise touching of the fragile screen with bare fingers
of one hand, while holding the phone in the second hand to control the software. That is hard to do if you wear warm or 
protective gloves, doing that in wet environment, or simply cannot use both hands for safety reasons. I'm offering an 
almost ready to implement Android solution which allows to control any software with 4 virtual arrow keys and 2 virtual 
buttons, using one hand only and without any need to touch anything on the phone, as well as making the app aware of the 
phone orientation in 3d space for additional (i.e. power saving / improved handling) functions. The quick proof of concept 
dirty code, utilizing the phone's gyroscope and accelerometer sensors for that, is provided.

# Use case implemented

The user wears the smartphone on the neck lanyard in a tough protective case and occasionally picks it up to the eyes 
to check his location, track, or add a way point. The phone screen turns on automatically on pick up, an goes off after 
a brief pause on returning back to hanging position.

User can scroll the map by 1/4 of the screen by tilting the corresponding side of the phone towards his/her eyes or outwards. E.g. flipping its top sharply away will scroll the map up (you can think of the map as of a fluid). I call it X-Y rocking as the gesture implies noticeable change of the screen's angle to your eyes, however the key is the speed of that gesture, not the angle.

If the map's zoom needed, the new Gyro UI mode can be selected from the rotary menu by rotating the phone screen (shaking
the top of the phone left or right) in the hand sharply left or right one time to bring it on, and repeating that gesture
to "slide" the necessary mode label (i.e. "Zoom") to the center of the menu. Menu disappears after a time-out or on any 
other Gyro Gesture. Now, X-Y rocking the phone forward will zoom out the map for one level, rocking it towards your eyes -
zoom it in. Rocking it left or right could be used for fine zooming, but that's not implemented.

Exactly the same sequence of gestures used to change the map type and to add way points. For way points there are 2 modes
implemented: rock forward registers it automatically, assigning just a number, rocking up will bring another Gyro UI 
control - Gyro Keyboard allowing to set the name for the waypoint. The UX is replicating the Garmin eTrex handheld GPS navigator 
way points naming screen with some improvements. Use the X-Y rocking to select a letter or a command button from it, rotate
to change the focus of the input or to save the name (while over the OK button). The keyboard is looped over its sides. 
The Underscore has a special functionality: it's converted to spaces and the excess spaces are trimmed, so the text must end 
with space, which allows to jump to the OK button from it on the keyboard. Also it can be used to delete parts of the name 
at its beginning or end (leading/trailing spaces are removed).

In order to aid user with the Gyro UI functionality available, the minimized rotary menu selection label is displayed at the top 
of the screen, in the bottom right corner of the screen a round icon reminding about allowed rocking directions is displayed as well (just by initial design, it should accompany the menu selection at the top, making a single UI element). Also there are two types of 
vibration feedback used to confirm UI changes, as visual feedback may not be reliably enough with the device moving in the 
hand.

With some practice the gestures are intuitive enough and natural to perform as soon as you have the limits and timeouts tuned
to your hands' dexterity with your particular phone/tablet once.

# Overview of the code

The provided code, though dirty, is fully buildable on top of your own Google Maps API key (will try to upload the working
binary as well). minSDK 10 was used. It represents a simple google mapping UI supplement with some rudimentary navigation 
options to demonstrate the functionality of the Gyro UI elements an to tune them for reliability. The map UI is built 
around the mock of the looped carousel menu, to select various essential navigation functions. The main menu of the app 
provides controls to tune up the Gyro UI algorithms sensitivity to the gestures manually in real time . Some of them must 
be transferred to the app's settings along with the user friendly tuning UI, similar to mine, as various sizes of the devices,
and various hands may need precise tuning of these triggers. The same should be done for the sleep function, as the lanyard
attachment point (as well as the preferred pocket orientation) may vary.

The sensors controller is coded as a simple events generating service to work after onPause() and for ease of business end 
implementation. It's residing in the "sensor" sub-module and self-sufficient, the rest is app dependent. The sensors data 
processing algorithm is very fast, as no matrix operations or any complex math needed at all.

The power saving trigger is sensing the gravity acceleration to be well along the predefined axis in with the certain 
direction, to reduce the number of false positives (still the delayed sleep needed to be implemented, as vigorous enough 
gestures can still trigger it). 

The wake-up is sensing gravity to maximize on the Z axis and being dominant for a while (put the screen horizontally
to wake up the phone).
