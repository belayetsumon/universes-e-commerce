/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.system.model;

/**
 *
 * @author libertyerp_local
 */
public class EndpointInfo {

    private String packageName;
    private String controller;
    private String method;
    private String httpMethod;
    private String path;

    public EndpointInfo() {
    }

    public EndpointInfo(String packageName, String controller, String method, String httpMethod, String path) {
        this.packageName = packageName;
        this.controller = controller;
        this.method = method;
        this.httpMethod = httpMethod;
        this.path = path;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
