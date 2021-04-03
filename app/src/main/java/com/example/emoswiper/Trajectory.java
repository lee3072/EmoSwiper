package com.example.emoswiper;

import java.util.ArrayList;

public class Trajectory {
    public static class Coordinate{
         float x;
         float y;
         float t;
         public Coordinate(float x, float y, float t) {
             this.x = x;
             this.y = y;
             this.t = t;
         }
    }
    public ArrayList<Coordinate> trajectory;
    public String emotion;
    public String image;

    public Trajectory(String emotion, String image) {
        this.emotion = emotion;
        this.image = image;
        this.trajectory = new ArrayList<>();
    }
    public Trajectory(ArrayList<Coordinate> trajectory) {
        this.trajectory = trajectory;
    }
}
