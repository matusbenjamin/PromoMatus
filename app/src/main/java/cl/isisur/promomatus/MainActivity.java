package cl.isisur.promomatus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageView imageCatedral;
    private ImageView imageLaguna;
    private CheckBox checkBoxCatedral, checkBoxLaguna;
    private Button buttonSeleccionar,buttonCancelarReserva;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private Calendar myCalendarCatedral = Calendar.getInstance();
    private Calendar myCalendarLaguna = Calendar.getInstance();
    private String selectedDateCatedral = "";
    private String selectedDateLaguna = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase Auth y Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Obtener referencias de los elementos del layout
        imageCatedral = findViewById(R.id.imageCatedral);
        imageLaguna = findViewById(R.id.imageLaguna);
        checkBoxCatedral = findViewById(R.id.checkBoxCatedral);
        checkBoxLaguna = findViewById(R.id.checkBoxLaguna);
        buttonSeleccionar = findViewById(R.id.buttonSeleccionar);
        buttonCancelarReserva = findViewById(R.id.buttonCancelar);

        // URLs de las imágenes
        String urlCatedral = "https://upload.wikimedia.org/wikipedia/commons/9/97/Catedral_de_Chill%C3%A1n_sin_antenas.jpg";
        String urlLaguna = "https://lh5.googleusercontent.com/p/AF1QipO4pFACxCIoZycrsI_wdsTNfZXOOIBUSSAzjkdW=w540-h312-n-k-no";

        // Cargar imágenes utilizando Glide
        Glide.with(this).load(urlCatedral).into(imageCatedral);
        Glide.with(this).load(urlLaguna).into(imageLaguna);

        // Configurar clic listeners para las imágenes
        imageCatedral.setOnClickListener(v -> launchMapsActivity(-36.60688056, -72.10209722, "Catedral de Chillán"));
        imageLaguna.setOnClickListener(v -> launchMapsActivity(-49.0723, -72.90375, "Laguna del Huemul"));


        buttonCancelarReserva.setOnClickListener(v -> cancelReservation());

        // Configurar clic listener para el botón de selección
        buttonSeleccionar.setOnClickListener(v -> saveReservation());
        updateCheckboxes();
    }

    private void launchMapsActivity(double latitude, double longitude, String title) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("title", title);
        startActivity(intent);
    }
    private void cancelReservation() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(MainActivity.this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userName = currentUser.getDisplayName();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        if (checkBoxCatedral.isChecked()) {
            cancelLocationReservation("Catedral de Chillán", todayDate, userName);
        }

        if (checkBoxLaguna.isChecked()) {
            cancelLocationReservation("Laguna del Huemul", todayDate, userName);
        }
    }

    private void cancelLocationReservation(String location, String date, String userName) {
        mDatabase.child("reservations").child(date).child(location).child(userName).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Reserva cancelada exitosamente", Toast.LENGTH_SHORT).show();
                        updateCheckboxes(); // Actualiza los checkboxes después de cancelar
                    } else {
                        Toast.makeText(MainActivity.this, "Error al cancelar la reserva: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveReservation() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(MainActivity.this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        Map<String, Object> reservationData = new HashMap<>();
        reservationData.put("Usuario", userName);
        reservationData.put("Fecha", todayDate);

        if (checkBoxCatedral.isChecked()) {
            saveLocationReservation("Catedral de Chillán", todayDate, userName, reservationData);
        }

        if (checkBoxLaguna.isChecked()) {
            saveLocationReservation("Laguna del Huemul", todayDate, userName, reservationData);
        }
    }

    private void saveLocationReservation(String location, String date, String userName, Map<String, Object> reservationData) {
        mDatabase.child("reservations").child(date).child(location).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || dataSnapshot.child(userName).exists()) {
                    reservationData.put(location, selectedDateCatedral);
                    mDatabase.child("reservations").child(date).child(location).child(userName).setValue(true)
                            .addOnCompleteListener(MainActivity.this, task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Reserva guardada exitosamente", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Error al guardar la reserva: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(MainActivity.this, location + " ya reservada para hoy", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error al verificar la reserva: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCheckboxes() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userName = currentUser.getDisplayName();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        // Comprobar disponibilidad para Catedral de Chillán
        mDatabase.child("reservations").child(todayDate).child("Catedral de Chillán")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isAvailable = !dataSnapshot.exists(); // Disponible si no hay reservas
                        if (isAvailable || (dataSnapshot.child(userName).exists())) {
                            checkBoxCatedral.setEnabled(true);
                            checkBoxCatedral.setChecked(dataSnapshot.child(userName).exists());
                        } else {
                            checkBoxCatedral.setEnabled(false);
                            checkBoxCatedral.setChecked(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Error al verificar la reserva: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Comprobar disponibilidad para Laguna del Huemul
        mDatabase.child("reservations").child(todayDate).child("Laguna del Huemul")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isAvailable = !dataSnapshot.exists(); // Disponible si no hay reservas
                        if (isAvailable || (dataSnapshot.child(userName).exists())) {
                            checkBoxLaguna.setEnabled(true);
                            checkBoxLaguna.setChecked(dataSnapshot.child(userName).exists());
                        } else {
                            checkBoxLaguna.setEnabled(false);
                            checkBoxLaguna.setChecked(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Error al verificar la reserva: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void checkAvailability(String location, String date, AvailabilityCallback callback) {
        mDatabase.child("reservations").child(date).child(location).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onCheckAvailable(!dataSnapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error al verificar disponibilidad: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Interface para comprobar disponibilidad
    interface AvailabilityCallback {
        void onCheckAvailable(boolean available);
    }
}
