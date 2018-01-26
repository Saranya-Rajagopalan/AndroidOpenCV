Readme.txt
)bject detection based on color
---------------------------------------------------------------------------------------------------
Objectives:	To detect a specific colored object in the live camera feed.
		Enclose the selected colored object with a rectangle and/or highlight it
		Change color of the selected object
		Save the image
---------------------------------------------------------------------------------------------------
Description: 	This application finds objects of a specific color and highlights them 
		by turning the background gray. It also has the feature to change the
		color of the selected object. The application saves the displayed image 
		to memory.

		Object detection based on color:
		A color selected from red, green or blue is used to detect a object in the
		camera frame.	
		
		Show/Box objects:
		The detected object is enclosed in a rectangle.
	
		Highlight object:
		A mask is used to convert the background to gray. Only the detected object is 
		displayed in color.

		Change color:
		The color of the object can be changed as per the selected color from yellow,
		magenta and cyan. 

		Save image:
		A file with the copy of displayed image can be saved to the memory.
---------------------------------------------------------------------------------------------------
Application:	Users use device camera to take pictures. But these pictures same as the 
		user sees it. This application will allow the user to store the images as 
		per his/her wish.
		This application can be used to save objects with specific color. It can 
		also be used to save images with different color. For example, an image of 
		flower can be stored with another color. This application can also be 
		used to see how a t-shirt will look in some other color. Regular pictures 
		can be saved too.
---------------------------------------------------------------------------------------------------
Features:	Object detection based on color:
		A color selected from red, green or blue is detected by converting the 
		image to HSV. This allows better comparison with the HSV range of each color 
		as compared to RGB pixel values.
		The HSV image is then filtered to get a binary mask. This mask is used to 
		differentiate between the desired object region and background.
		
		Show/Box objects:
		Using morphological operations of opening and closing (sequence of erode
		and dilate operations) the noise is reduced and the thin gaps are 
		solidified. This binary output is used to find object regions in the image
		and stored in a vector using the connectedComponentsWithStats method. The 
		rectangle dimensions are computed and drawn using the details in vector 
		rows.
	
		Highlight object:
		Using a mask, the background is converted to a gray image. An inverted mask 
		allows to obtain object region in the image. The two images are then combined 
		to display the highlighted object.

		Change color:
		The color of the object modified using masking and changing the pixel values.
		The pixels in the masked region isolating the object is set to specific value
		which displays changed color.

		Save image:
		The image matrix is converted to a bitmap object. File storage functions 
		like create, open and close are used to store the the bitmap image.
		It also creates an application directory in the storage path. The name of the
		file is formatted according to date and time so that the files are not replaced
		after every run.
---------------------------------------------------------------------------------------------------
Future work:	1. The color picked by the touch function can be used to detect objects and
		   as the changed color.
		2. Hough circles can also be implemented to enclose the objects in circles.
		3. Feature detection can be used to detect objects and then change their color.
		4. A function can be implemented to store the image if a particlar color or 
		   object is detected.
		5. The app requires some time to preocess the image and the button clicks.
		   A few algorithms can be implemented to make this application compute the 
		   results fast.
---------------------------------------------------------------------------------------------------
Permissions:	1. After installing the application, open the application settings in 
		2. Apps settings. Give permissions to use camera and storage.
		3. The application will exit if the camera permissions are not granted.
		4. Application executes even if storage permissions are not set

Execution:	After installing the application and setting permissions, click on 
		MyApplication.
		The orientation of the application is set to landscape so that more area 
		is captured. The touch co-ordinates are modified accordingly so that the
		Part B touch color functionality works successfully.

		There are a few radio buttons and switches in the application GUI
		1. Radio buttons on the left (Select a color to highlight)
		   You can select anyone among Red, Green and Blue.
		   This is required to use the application features.
		2. Radio buttons on the right(Change selected color to)
		   You can select anyone among Yellow, Magenta and Cyan.
		3. Highlight Switch
		   If selected, highlights the object by making the background gray
		   Default: Not selected
		4. Show objects Switch
		   If selected, detected objects are enclosed in a rectangle
		   Default: Selected
		5. Save Button
		   When clicked, saves image to memory
---------------------------------------------------------------------------------------------------
Sample:		Input and Output screenshots images stored in the "Samples" can be used as live 
		feed for the camera.
---------------------------------------------------------------------------------------------------
Output:		On screen output
		"XX selected"			Indicates color selected from left radio buttons
		"Highlighting color"		Indicates highlight selected
		"Not highlighting!"		Indicates highlight not selected
		"Objects boxed"			Indicates show objects selected
		"Objects not displayed"		Indicates show objects not selected
		"Saving.."			Indicates file being saved
		"Do not have save permissions"	No storage permissions
---------------------------------------------------------------------------------------------------
References:
1. https://opencv-srf.blogspot.ro/2010/09/object-detection-using-color-seperation.html
2. https://stackoverflow.com/questions/32522989/opencv-better-detection-of-red-color
3. https://docs.opencv.org/3.2.0/d0/d86/tutorial_py_image_arithmetics.html 
4. https://www.tutorialspoint.com/java_dip/eroding_dilating.htm
5. http://answers.opencv.org/question/96443/find-rectangle-from-image-in-android/
6. https://stackoverflow.com/questions/14072761/how-to-read-value-of-the-each-pixel-as-rgb-values-
   from-mat-object-m-in-opencv
7. https://stackoverflow.com/questions/26491040/why-is-my-apk-name-generic
8. http://answers.opencv.org/question/68206/saving-an-image-using-opencv-android/
9. https://stackoverflow.com/questions/32846735/find-circles-image-processing-in-opencv-java
10.http://answers.opencv.org/question/96443/find-rectangle-from-image-in-android/
----------------------------------------EOF-----------------------------------------------
