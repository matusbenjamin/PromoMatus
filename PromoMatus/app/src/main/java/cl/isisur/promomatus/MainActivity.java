package cl.isisur.promomatus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private ImageView imageCatedral;
    private ImageView imageLaguna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtén las referencias de los ImageView del layout
        imageCatedral = findViewById(R.id.imageCatedral);
        imageLaguna = findViewById(R.id.imageLaguna);

        // URLs de las imágenes
        String urlCatedral = "https://upload.wikimedia.org/wikipedia/commons/9/97/Catedral_de_Chill%C3%A1n_sin_antenas.jpg";
        String urlLaguna = "https://lh5.googleusercontent.com/p/AF1QipO4pFACxCIoZycrsI_wdsTNfZXOOIBUSSAzjkdW=w540-h312-n-k-no";

        // Cargar imágenes utilizando Glide
        Glide.with(this)
                .load(urlCatedral)
                .into(imageCatedral);

        Glide.with(this)
                .load(urlLaguna)
                .into(imageLaguna);

        // Configurar clic listeners para las imágenes
        imageCatedral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMapsActivity(-36.60688056, -72.10209722, "Catedral de Chillán");
            }
        });

        imageLaguna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMapsActivity(-49.0723, -72.90375, "Laguna del Huemul");
            }
        });
    }

    // Método para lanzar la actividad del mapa con las coordenadas y el título
    private void launchMapsActivity(double latitude, double longitude, String title) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("title", title);
        startActivity(intent);
    }
}
