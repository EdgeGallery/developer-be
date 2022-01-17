package org.edgegallery.developer.util.helmcharts.k8sobject;

public class Tets {
    public static void main(String[] args) {
        String str = "port: 9997.0";
        System.out.println(str.substring(0,str.lastIndexOf(".")));
    }
}
