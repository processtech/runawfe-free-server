package ru.runa.wfe.redmine.api.impl;

import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;

import ru.runa.wfe.redmine.api.RedmineConfig;

public class RedmineConfigImpl implements RedmineConfig {
     private String                  uri            = null;
     private String                  login          = null;
     private String                  password       = null;
     private RedmineManager          redmineManager = null;
     
     public void setUri(String uri) {
         this.uri = uri;
     }
     
     public String getUri() {
         return this.uri;
     }
     
     public void setLogin(String login) {
         this.login = login;
     }
     
     public String getLogin() {
         return this.login;
     }
     
     public void setPassword(String password) {
         this.password = password;
     }
     
     public String getPassword() {
         return this.password;
     }
     
     public synchronized RedmineManager getRedmineManager() {
         if (this.redmineManager == null) {
             this.redmineManager = 
                 RedmineManagerFactory.createWithUserAuth(
                     this.getUri(), 
                     this.getLogin(), 
                     this.getPassword());
         }
         
         return this.redmineManager;
     }
}
