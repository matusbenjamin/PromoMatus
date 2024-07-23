package cl.isisur.promomatus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextEmail, editTextPassword;
    private Button buttonRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Inicializar vistas
        editTextName = findViewById(R.id.editTextText);
        editTextPhone = findViewById(R.id.editTextText2);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress2);
        editTextPassword = findViewById(R.id.editTextTextPassword2);
        buttonRegister = findViewById(R.id.button);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configurar el listener del botón de registro
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
    }

    private void saveUserData() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity2.this, "Por favor ingrese todos los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un nuevo usuario con correo y contraseña
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro exitoso
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Guardar nombre en el perfil del usuario
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            // Guardar datos adicionales en la base de datos
                                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("name", name);
                                            userData.put("phone", phone);
                                            userData.put("email", email);

                                            userRef.setValue(userData)
                                                    .addOnCompleteListener(dbTask -> {
                                                        if (dbTask.isSuccessful()) {
                                                            // Redirigir a MainActivity3
                                                            Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            // Si falla al guardar en la base de datos
                                                            Toast.makeText(MainActivity2.this, "Error al guardar los datos: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            // Si falla al actualizar el perfil
                                            Toast.makeText(MainActivity2.this, "Error al actualizar perfil: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        // Si el registro falla, muestra un mensaje al usuario.
                        Toast.makeText(MainActivity2.this, "Error al registrarse: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
