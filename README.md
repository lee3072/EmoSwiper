# EmoSwiper

### About the Project
This project aims to classify the immediate emotional state of the user when swiping the screen. 
To induce emotion, our Android app displays each image from a public dataset with annotated emotion labels for 5 seconds, and then the user swipes the screen to view the next image. 
In this way, the swipe event is affected by the image that the user just has seen. 
We trained Bi-LSTM model to predict the emotion label of the image using the trajectory of the swipe, recorded using our Android app. 
We limit our classification to be binary, that is whether the emotion is positive or negative.

[<img src="https://i.ibb.co/wpKfGVp/Screenshot-20210403-190249.jpg" width="180"/>](https://i.ibb.co/wpKfGVp/Screenshot-20210403-190249.jpg)

### How to Play Around?
1. Set a desired value of MAX_TRAIN_SIZE constant in the Android app. This is how many swipe data samples that you want to collect
2. Compile the Android app and collect your swipe data. You need to watch each image for 5 seconds, and then swipe the screen to view the next one
3. After all data is collected, you can retreive a EmoSwipe.csv file from your phone
4. Copy the file into the python program in /train and run the model
5. Copy the serialized model from /train to /assets in the Android app
6. Compile again, and use predict mode on the app to detect swipe emotion from the phone (to be fixed).

### Test Results
- We trained with 300 training samples, 100 validation samples, and test on 93 test samples.
- The test result is as follows: 
- Accuracy 0.7204
- Confusion Matrix: 
-
| Pred   \  Truth | Positive | Negative|
| :-------------: | :------: | :-----: |
|     Positive    |    40    |   18    |
|     Negative    |    8     |   27    |

### Issues
The Android app crashes when loading the serialized model. So only offline testing is available currently.

### References
- Maramis, C., et al. "Emotion recognition from haptic touch on android device screens." International Conference on Biomedical and Health Informatics. Springer, Singapore, 2017.
- Balducci, Fabrizio, et al. "Affective states recognition through touch dynamics." Multimedia Tools and Applications 79.47 (2020): 35909-35926.
- Machajdik, J., & Hanbury, A. (2010, October). Affective image classification using features inspired by psychology and art theory. In Proceedings of the 18th ACM international conference on Multimedia (pp. 83-92)
