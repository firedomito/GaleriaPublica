package com.example.galeriapublica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    static int RESULT_REQUEST_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivityViewModel vm = new ViewModelProvider(this).get(MainActivityViewModel.class);

        bottomNavigationView = findViewById(R.id.btNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //Escolhe se vai ser mostrado em grid ou em lista
                switch (item.getItemId()) {
                    case R.id.gridViewOp:
                        GridViewFragment gridViewFragment = GridViewFragment.newInstance();
                        setFragment(gridViewFragment);

                        //Caso opção selecionada seja grid esse codigo será guardado
                        vm.setNavigationOpSelected(R.id.gridViewOp);


                        break;
                    case R.id.listViewOp:
                        ListViewFragment listViewFragment = ListViewFragment.newInstance();
                        setFragment(listViewFragment);

                        //Caso opção selecionada seja list esse codigo será guardado
                        vm.setNavigationOpSelected(R.id.listViewOp);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Pede permissão para ter ler o arquivo externo
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        checkForPermissions(permissions);
    }

    void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragContainer, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    //Funcao que carrega imagens
    void loadImageData() {
        Log.i("Galeria publica", "Imagens carregadas da galeria publica do celular.");

        MainActivityViewModel vm = new ViewModelProvider(this).get(MainActivityViewModel.class);
        HashMap<Long, ImageData> imageDataList = vm.getImageDataList();

        int w = (int)getResources().getDimension(R.dimen.im_width);
        int h = (int)getResources().getDimension(R.dimen.im_height);

        String[] projection = new String[]{MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.SIZE
        };
        String selection = null;
        String selectionArgs[] = null;
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " ASC";

        try {
            //Da acesso as imagens
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);

            //Se ainda existir imagem a ser navegada obtem os dados
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);

                //Verifica se o id já existe no hashmap
                if (!imageDataList.containsKey(id)) {
                    String name = cursor.getString(nameColumn);
                    int dateAdded = cursor.getInt(dateAddedColumn);
                    int size = cursor.getInt(sizeColumn);

                    Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);


                    Bitmap thumb = Util.getBitmap(MainActivity.this, imageUri, w, h);


                    imageDataList.put(id, new ImageData(thumb, name, new Date(dateAdded), size));
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Seleciona a opção previamente guardada
        bottomNavigationView.setSelectedItemId(vm.getNavigationOpSelected());
    }



    //Verifica se tem as permissões necessarias para executar o app
    private void checkForPermissions(List<String> permissions) {
        List<String> permissionsNotGranted = new ArrayList<>();

        //Verifica se já tem a permissão necessaria
        for (String permission : permissions) {
            //Coloca na lista as permissões que faltam
            if (!hasPermission(permission)) {
                permissionsNotGranted.add(permission);
            }
        }
        if (permissionsNotGranted.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]), RESULT_REQUEST_PERMISSION);
            }
        }
        else {
            loadImageData();
        }
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    //Verificação de rejeição de permissão
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        List<String> permissionsRejected = new ArrayList<>();

        if (requestCode == RESULT_REQUEST_PERMISSION) {
            for (String permission : permissions) {
                if (!hasPermission(permission)) {
                    permissionsRejected.add(permission);
                }
            }
        }

        //Insiste no pedido de permissão
        if (permissionsRejected.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    new AlertDialog.Builder(MainActivity.this).
                            setMessage("Para utilização do app conceda as permissões").
                            setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), RESULT_REQUEST_PERMISSION);
                                }
                            }).create().show();
                }

            }
        }
        else {
            loadImageData();
        }

    }
}