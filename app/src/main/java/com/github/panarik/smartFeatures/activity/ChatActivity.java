package com.github.panarik.smartFeatures.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.github.panarik.smartFeatures.R;
import com.github.panarik.smartFeatures.data.chat.ChatMessage;
import com.github.panarik.smartFeatures.data.chat.ChatMessageAdapter;
import com.github.panarik.smartFeatures.data.chat.ChatUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.google.firebase.database.FirebaseDatabase.getInstance;

import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends AppCompatActivity {

    private ListView messageListView;
    private ChatMessageAdapter adapter;
    private ProgressBar progressBar;
    private ImageButton chat_messageSendPhotoImageButton;
    private Button chat_messageSendButton;
    private EditText chat_messageEditText;
    private String userName;

    //БД Firebase
    FirebaseDatabase database;
    DatabaseReference messagesDatabaseReference; //БД сообщений
    DatabaseReference usersDatabaseReference; //БД пользователей
    //прослушиваем изменения БД
    ChildEventListener messagesChildEventListener; //слушаем узел сообщений
    ChildEventListener usersChildEventListener; //слушаем узел пользователей

    //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //инициализируем БД Firebase
        database = getInstance();
        //инициализируем узлы в БД
        messagesDatabaseReference = database.getReference().child("messages");
        usersDatabaseReference = database.getReference().child("users");

        progressBar = findViewById(R.id.progressBar);
        messageListView = findViewById(R.id.messageListView);
        chat_messageSendPhotoImageButton = findViewById(R.id.chat_messageSendPhotoImageButton);
        chat_messageSendButton = findViewById(R.id.chat_messageSendButton);
        chat_messageEditText = findViewById(R.id.chat_messageEditText);


        //получаем userName из MainActivity
        get_UserName_from_MainActivity();


        //создаем новый адаптер и передаем ему ArrayList
        List<ChatMessage> chatMessages = new ArrayList<>();
        adapter = new ChatMessageAdapter(this, R.layout.chat_message_item, chatMessages);
        messageListView.setAdapter(adapter);

        //изначально progressBar невидим
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        //прошлушиваем ввод текста для отображения кнопки "Отправить"
        chat_messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    chat_messageSendButton.setEnabled(true);
                } else {
                    chat_messageSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        //ограничиваем длину сообщения
        chat_messageEditText.setFilters
                (new InputFilter[]
                        {new InputFilter.LengthFilter(500)}
                );


        chat_messageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //собираем текст
                ChatMessage message = new ChatMessage();
                message.setText(chat_messageEditText.getText().toString());
                message.setName(userName);
                message.setImageUrl(null);

                //отправляем на сервер с помощью .push()
                messagesDatabaseReference.push().setValue(message);

                chat_messageEditText.setText("");
            }
        });


        chat_messageSendPhotoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


        //прослушаваем изменения в БД (узел юзеров)
        usersChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //получаем всех пользователей
                ChatUser user = snapshot.getValue(ChatUser.class);
                //находим нужного пользователя (сравниваем текущего с БД)
                if (user.getUserId()
                        .equals(
                                FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    userName = user.getUserName();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        //прикрепляем listener к БД users
        usersDatabaseReference.addChildEventListener(usersChildEventListener);



        //прослушаваем изменения в БД (узел сообщений)
        messagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //получаем снимок (snapshot)
                ChatMessage message = snapshot.getValue(
                        //указываем где распознавать значения
                        ChatMessage.class);
                //итого, получаем объект с полями, и устанавливаем его в Адаптер
                adapter.add(message);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        //прикрепляем listener к БД messages.
        messagesDatabaseReference.addChildEventListener(messagesChildEventListener);

    }


    private void get_UserName_from_MainActivity() {
        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("userName");
        } else {
            userName = "Default User";
        }
    }


    //активация меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    //пункты меню
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.main_sign_out:
                //выйти из учетной записи
                FirebaseAuth.getInstance().signOut();
                //переход на экран авторизации
                Intent goToSignInActivity = new Intent(ChatActivity.this, SignInActivity.class);
                startActivity(goToSignInActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void goToMainActivity(View view) {
        Intent goToMainActivity = new Intent(this, MainActivity.class);
        startActivity(goToMainActivity);
    }

}
