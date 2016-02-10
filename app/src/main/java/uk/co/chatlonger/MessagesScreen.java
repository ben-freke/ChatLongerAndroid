package uk.co.chatlonger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MessagesScreen extends AppCompatActivity {
    private DatabaseConnector connector;
    private Context conversationContext;
    private boolean firstBoot = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        build();
    }

    protected void onResume()
    {
        super.onResume();
        build();
    }

    protected void build()
    {
        if (firstBoot){
            setContentView(R.layout.activity_messages_screen);
            setTitle("Messages");
            connector = new DatabaseConnector(this);
            conversationContext = this;
            getConversations();
            firstBoot = false;
        } else {
            getConversations();
        }

    }

    public void onClick(View v) {

        Intent intent = new Intent(this, NewConversation.class);
        //intent.putExtra("conversationID", v.getContentDescription().toString());
        startActivity(intent);

    }

    public void getConversations(){
        LinearLayout ll = (LinearLayout) findViewById(R.id.convLayout);
        ll.removeAllViews();
        String[][] conversations = connector.getConversations();
        if (conversations != null){
            for (int i = 0; i < conversations.length; i++)
            {
                putConversation(conversations[i][0], conversations[i][1]);
            }
        }


    }

    private void putConversation(String name, final String id)
    {
        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.convLayout);

        LinearLayout convoLayout = new LinearLayout(conversationContext);
        convoLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams convoLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        convoLayout.setLayoutParams(convoLayoutParams);
        convoLayout.setContentDescription(id);
        convoLayout.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                intent.putExtra("conversationID", String.valueOf(id));
                startActivity(intent);
            }
        });
        convoLayout.setPadding(0, 40, 0, 0);
        DisplayMetrics metrics =conversationContext.getResources().getDisplayMetrics();
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());


        ImageView figure = new ImageView(conversationContext);
        LinearLayout.LayoutParams figureLayoutParams = new LinearLayout.LayoutParams((int)pixels, (int)pixels);
        figure.setLayoutParams(figureLayoutParams);
        figure.setImageResource(R.drawable.figure);

        convoLayout.addView(figure);

        LinearLayout.LayoutParams conversationLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        conversationLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        TextView conversation = new TextView(conversationContext);
        conversation.setLayoutParams(conversationLayoutParams);
        conversation.setTextColor(Color.BLACK);
        conversation.setText(name);
        conversation.setTextSize(25);
        conversation.setGravity(Gravity.LEFT);
        conversation.setPadding(40, 0, 0, 0);
        convoLayout.addView(conversation);

        parentLayout.addView(convoLayout);
    }


}
