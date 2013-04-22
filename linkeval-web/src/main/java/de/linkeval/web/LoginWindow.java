package de.linkeval.web;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class LoginWindow extends Window
{
	private Button btnLogin = new Button("Login");
    private TextField login = new TextField ( "Username");
    private PasswordField password = new PasswordField ( "Password");
    private AbsoluteLayout layout= new AbsoluteLayout();

    public LoginWindow ()
    {
       super("Authentication Required !");
/*        UI.getCurrent(). setName ( "login" );
*/        initUI();
    }

    private void initUI ()
    {
    	layout.addComponent ( new Label ("Please login in order to use the application") );
        layout.addComponent ( new Label () );
        layout.addComponent ( login );
        layout.addComponent ( password );
        layout.addComponent ( btnLogin );
        this.setContent(layout);
    }
}
