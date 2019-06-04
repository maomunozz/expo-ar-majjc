package com.univalle.appmuseo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.PixelCopy;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AugmentedPlatosActivity extends AppCompatActivity implements View.OnClickListener {
    // PopUps de informacion
    Dialog popupDescripcion;
    Dialog popupGallery;
    Dialog popupVideo;
    Dialog popupInstrucciones;

    HorizontalScrollView scrollView;

    // Fragment para la realidad aumentada
    ArFragment arFragmentPlatos;

    // Rendereables para los modelos de las piezas
    private ModelRenderable pieza2161Rendereable, pieza2219Rendereable, pieza2229Rendereable, pieza3227Rendereable, pieza3274Rendereable;

    // Imagenes para el scroll de las piezas a seleccionar
    ImageView pieza2161, pieza2219, pieza2229, pieza3227, pieza3274;

    // Array de vistas
    View arrayView[];

    // Variable para el seleccionador de piezas, por default esta en 1
    int selected = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented_platos);

        //Esconder la barra de navegacion
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        scrollView = (HorizontalScrollView) findViewById(R.id.scrollGsllery);

        //Boton flotante para tomar fotos y llamado a su metodo
        FloatingActionButton fab_take_photo = (FloatingActionButton) findViewById(R.id.takePhoto);
        fab_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        //Boton flotante para tomar fotos y llamado a su metodo
        FloatingActionButton fab_back_home = (FloatingActionButton) findViewById(R.id.backHome);
        fab_back_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backHome();
            }
        });

        // Se le asigna la el fragment creado a la variable
        arFragmentPlatos = (ArFragment)getSupportFragmentManager().findFragmentById(R.id.sceneform_ux_fragment);

        // Se a las variables de imagen creadas, cada una de las vistas
        pieza2161 = (ImageView)findViewById(R.id.pieza_2161);
        pieza2219 = (ImageView)findViewById(R.id.pieza_2219);
        pieza2229 = (ImageView)findViewById(R.id.pieza_2229);
        pieza3227 = (ImageView)findViewById(R.id.pieza_3227);
        pieza3274 = (ImageView)findViewById(R.id.pieza_3274);

        // Metodo que inicia el array
        setArray();

        // Metodo que agrega los oyentes a cada imagen del scroll
        setClickListener();

        // Metodo que crea el modelo en la posicion seleccionada
        setupModel();

        // PopUps de informacion
        popupDescripcion = new Dialog(this);
        popupGallery = new Dialog(this);
        popupVideo = new Dialog(this);
        popupInstrucciones = new Dialog(this);
        showVideo();
    }



    //Metodo para volver al inicio
    private void backHome(){
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        finish();
    }

    // Metodo para generar la ruta donde se guardaran las fotos y el nombre de la foto
    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" + date + "_screenshot.jpg";
    }

    // Metodo para guardar la foto
    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {

        File out = new File(filename);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename);
             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
        } catch (IOException ex) {
            throw new IOException("Failed to save bitmap to disk", ex);
        }
    }

    // Metodo para tomar la fotografia
    private void takePhoto() {
        final String filename = generateFilename();
        ArSceneView view = arFragmentPlatos.getArSceneView();

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        PixelCopy.request(view, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveBitmapToDisk(bitmap, filename);
                } catch (IOException e) {
                    Toast toast = Toast.makeText(AugmentedPlatosActivity.this, e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Foto guardada", Snackbar.LENGTH_LONG);
                snackbar.setAction("Abrir en fotos", v -> {
                    File photoFile = new File(filename);

                    Uri photoURI = FileProvider.getUriForFile(AugmentedPlatosActivity.this,
                            AugmentedPlatosActivity.this.getPackageName() + ".univalle.appmuseo.name.provider",
                            photoFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
                    intent.setDataAndType(photoURI, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);

                });
                snackbar.show();
            } else {
                Toast toast = Toast.makeText(AugmentedPlatosActivity.this,
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }

    // Metodo que mestra la descripcion
    public void showDescription(View view, Integer image , String txtTitulo, String txtFecha, String txtDim, String txtArtefacto, String txtTipo, String txtDescripcion){

        TextView btnclose;
        ImageView imagen;
        TextView titulo;
        TextView fecha;
        TextView dimensiones;
        TextView artefacto;
        TextView tipo;

        TextView descripcion;

        popupDescripcion.setContentView(R.layout.popup_info);

        imagen = popupDescripcion.findViewById(R.id.imagePiece);
        imagen.setImageResource(image);

        titulo = popupDescripcion.findViewById(R.id.tituloPiece);
        titulo.setText(txtTitulo);

        fecha = popupDescripcion.findViewById(R.id.fechaPiece);
        fecha.setText(txtFecha);

        dimensiones = popupDescripcion.findViewById(R.id.dimensionesPiece);
        dimensiones.setText(txtDim);

        artefacto = popupDescripcion.findViewById(R.id.artefactoPiece);
        artefacto.setText(txtArtefacto);

        tipo = popupDescripcion.findViewById(R.id.tipoytecPiece);
        tipo.setText(txtTipo);

        descripcion = popupDescripcion.findViewById(R.id.descriptionPiece);
        descripcion.setText(txtDescripcion);

        btnclose = popupDescripcion.findViewById(R.id.btn_close);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupDescripcion.dismiss();
            }
        });
        popupDescripcion.show();
    }

    public void showVideo(View view, Uri uriVideo){

        TextView btnclose;
        VideoView video;

        popupVideo.setContentView(R.layout.popup_video);

        video = popupVideo.findViewById(R.id.videoView);
        video.setVideoURI(uriVideo);
        video.start();

        btnclose = popupVideo.findViewById(R.id.btn_close_video);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupVideo.dismiss();
            }
        });
        popupVideo.show();
    }

    public void showVideo() {
        TextView btnclose;
        popupInstrucciones.setContentView(R.layout.popup_instrucciones);
        btnclose = popupInstrucciones.findViewById(R.id.button_ok);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupInstrucciones.dismiss();
            }
        });
        popupInstrucciones.show();
    }

    public void showGallery(View view, Integer imgSuperior, Integer imgPrimarios, Integer imgAglomeracion){

        ImageView imagenSuperior, imagenPrimarios, imagenAglomeracion;
        TextView btnclose;

        popupGallery.setContentView(R.layout.popup_gallery);

        imagenSuperior = popupGallery.findViewById(R.id.imageSuperior);
        imagenSuperior.setImageResource(imgSuperior);

        imagenPrimarios = popupGallery.findViewById(R.id.imagePrimarios);
        imagenPrimarios.setImageResource(imgPrimarios);

        imagenAglomeracion = popupGallery.findViewById(R.id.imageAglomeracion);
        imagenAglomeracion.setImageResource(imgAglomeracion);

        btnclose = popupGallery.findViewById(R.id.btn_close_gallery);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupGallery.dismiss();
            }
        });
        popupGallery.show();
    }

    // Metodo que inicializa el array con las variables de vistas de las piezas
    private void setArray(){
        arrayView = new View[]{
                pieza2161, pieza2219, pieza2229, pieza3227, pieza3274
        };
    }

    // Metodo que construye los rendereables con los modelos de cada pieza
    public void setupModel(){

        ModelRenderable.builder()
                .setSource(this, R.raw.piece2219)
                .build().thenAccept(rendereable -> pieza2219Rendereable = rendereable)
                .exceptionally(
                        throwable -> {
                            Toast.makeText(this, "No se puede cargar el modelo de la pieza 2219", Toast.LENGTH_SHORT).show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.piece2161)
                .build().thenAccept(rendereable -> pieza2161Rendereable = rendereable)
                .exceptionally(
                        throwable -> {
                            Toast.makeText(this, "No se puede cargar el modelo de la pieza 2161", Toast.LENGTH_SHORT).show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.piece2229)
                .build().thenAccept(rendereable -> pieza2229Rendereable = rendereable)
                .exceptionally(
                        throwable -> {
                            Toast.makeText(this, "No se puede cargar el modelo de la pieza 2229", Toast.LENGTH_SHORT).show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.piece3227)
                .build().thenAccept(rendereable -> pieza3227Rendereable = rendereable)
                .exceptionally(
                        throwable -> {
                            Toast.makeText(this, "No se puede cargar el modelo de la pieza 3227", Toast.LENGTH_SHORT).show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.piece3274)
                .build().thenAccept(rendereable -> pieza3274Rendereable = rendereable)
                .exceptionally(
                        throwable -> {
                            Toast.makeText(this, "No se puede cargar el modelo de la pieza 3274", Toast.LENGTH_SHORT).show();
                            return null;
                        });
    }

    public void animateScrollView(){
        //scrollView.setAlpha(0.0f);

        scrollView.animate()
                .translationY(scrollView.getHeight())
                //.alpha(1.0f)
                .setListener(null);
    }

    public void utilCreateModel (ModelRenderable modelRenderable, AnchorNode anchorNode, String tituloPieza, String fechaPieza,
                                 String dimensionesPieza, String artefactoPieza, String tipoPieza, String descripcionPieza, String uriData, String uriSuperiorData,
                                 String uriPrimariosData, String uriAglomeracionData, int videomuestraData, int selectData){

        selected = 0;
        TransformableNode pieza = new TransformableNode(arFragmentPlatos.getTransformationSystem());
        pieza.setParent(anchorNode);
        pieza.setRenderable(modelRenderable);
        pieza.select();

        arFragmentPlatos.getArSceneView().getPlaneRenderer().setVisible(false);

        String uri = uriData;
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());

        String uriSuperior = uriSuperiorData;
        int imageSuperior = getResources().getIdentifier(uriSuperior, null, getPackageName());

        String uriPrimarios = uriPrimariosData;
        int imagePrimarios = getResources().getIdentifier(uriPrimarios, null, getPackageName());

        String uriAglomeracion = uriAglomeracionData;
        int imageAglomeracion = getResources().getIdentifier(uriAglomeracion, null, getPackageName());

        String videoPath = "android.resource://" + getPackageName() + "/" + videomuestraData;
        Uri videoUri = Uri.parse(videoPath);

        showScroll(anchorNode, selectData);
        addButtonInfo(anchorNode,pieza, imageResource, tituloPieza, fechaPieza, dimensionesPieza, artefactoPieza, tipoPieza, descripcionPieza);
        addGalleryInfo(anchorNode,pieza, imageSuperior, imagePrimarios, imageAglomeracion);
        addButtonVideo(anchorNode,pieza,videoUri);
        animateScrollView();
    }

    // Metodo que crea el ancla para el modelo dependiendo de la seleccion en el scroll de piezas
    public void createModel(AnchorNode anchorNode, int objectselected){

        if (objectselected == 1)
        {
            String tituloPieza = getString(R.string.name_p2161);
            String fechaPieza = getString(R.string.fecha_p2161);
            String dimensionesPieza = getString(R.string.dim_p2161);
            String artefactoPieza = getString(R.string.art_p2161);
            String tipoPieza = getString(R.string.tipo_p2161);
            String descripcionPieza = getString(R.string.desc_p2161);

            String uri = "@drawable/pieza_2161";
            String uriSuperior = "@drawable/ic_vistasuperior2161";
            String uriPrimarios = "@drawable/ic_primarios2161";
            String uriAglomeracion = "@drawable/ic_aglomeracion2161";

            int intVideoMuestra = R.raw.videomuestra;
            utilCreateModel(pieza2161Rendereable, anchorNode, tituloPieza, fechaPieza, dimensionesPieza, artefactoPieza, tipoPieza, descripcionPieza, uri, uriSuperior, uriPrimarios, uriAglomeracion, intVideoMuestra, 2);
        }

        if (objectselected == 2)
        {
            String tituloPieza = getString(R.string.name_p2219);
            String fechaPieza = getString(R.string.fecha_p2219);
            String dimensionesPieza = getString(R.string.dim_p2219);
            String artefactoPieza = getString(R.string.art_p2219);
            String tipoPieza = getString(R.string.tipo_p2219);
            String descripcionPieza = getString(R.string.desc_p2219);

            String uri = "@drawable/pieza_2219";
            String uriSuperior = "@drawable/ic_vistasuperior2219";
            String uriPrimarios = "@drawable/ic_primarios2219";
            String uriAglomeracion = "@drawable/ic_aglomeracion2219";

            int intVideoMuestra = R.raw.videomuestra;
            utilCreateModel(pieza2219Rendereable, anchorNode, tituloPieza, fechaPieza, dimensionesPieza, artefactoPieza, tipoPieza, descripcionPieza, uri, uriSuperior, uriPrimarios, uriAglomeracion, intVideoMuestra, 3);
        }

        if (objectselected == 3)
        {
            String tituloPieza = getString(R.string.name_p2229);
            String fechaPieza = getString(R.string.fecha_p2229);
            String dimensionesPieza = getString(R.string.dim_p2229);
            String artefactoPieza = getString(R.string.art_p2229);
            String tipoPieza = getString(R.string.tipo_p2229);
            String descripcionPieza = getString(R.string.desc_p2229);

            String uri = "@drawable/pieza_2229";
            String uriSuperior = "@drawable/vistasuperior2229";
            String uriPrimarios = "@drawable/ic_primarios2229";
            String uriAglomeracion = "@drawable/ic_aglomeracion2229";

            int intVideoMuestra = R.raw.videomuestra;
            utilCreateModel(pieza2229Rendereable, anchorNode, tituloPieza, fechaPieza, dimensionesPieza, artefactoPieza, tipoPieza, descripcionPieza, uri, uriSuperior, uriPrimarios, uriAglomeracion, intVideoMuestra, 4);
        }

        if (objectselected == 4)
        {
            String tituloPieza = getString(R.string.name_p3227);
            String fechaPieza = getString(R.string.fecha_p3227);
            String dimensionesPieza = getString(R.string.dim_p3227);
            String artefactoPieza = getString(R.string.art_p3227);
            String tipoPieza = getString(R.string.tipo_p3227);
            String descripcionPieza = getString(R.string.desc_p3227);

            String uri = "@drawable/pieza_3227";
            String uriSuperior = "@drawable/ic_vistasuperior3227";
            String uriPrimarios = "@drawable/ic_primarios3227";
            String uriAglomeracion = "@drawable/ic_aglomeracion3227";

            int intVideoMuestra = R.raw.videomuestra;
            utilCreateModel(pieza3227Rendereable, anchorNode, tituloPieza, fechaPieza, dimensionesPieza, artefactoPieza, tipoPieza, descripcionPieza, uri, uriSuperior, uriPrimarios, uriAglomeracion, intVideoMuestra, 7);
        }

        if (objectselected == 5)
        {
            String tituloPieza = getString(R.string.name_p3274);
            String fechaPieza = getString(R.string.fecha_p3274);
            String dimensionesPieza = getString(R.string.dim_p3274);
            String artefactoPieza = getString(R.string.art_p3274);
            String tipoPieza = getString(R.string.tipo_p3274);
            String descripcionPieza = getString(R.string.desc_p3274);

            String uri = "@drawable/pieza_3274";
            String uriSuperior = "@drawable/ic_vistasuperior3274";
            String uriPrimarios = "@drawable/ic_primarios3274";
            String uriAglomeracion = "@drawable/ic_aglomeracion3274";

            int intVideoMuestra = R.raw.videomuestra;
            utilCreateModel(pieza3274Rendereable, anchorNode, tituloPieza, fechaPieza, dimensionesPieza, artefactoPieza, tipoPieza, descripcionPieza, uri, uriSuperior, uriPrimarios, uriAglomeracion, intVideoMuestra, 8);
        }
    }

    public void showScroll (AnchorNode anchorNode, int pieceSelected){
        //Boton flotante para mostrar el scroll
        FloatingActionButton fab_show_scroll = (FloatingActionButton) findViewById(R.id.showScroll);
        fab_show_scroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anchorNode.setParent(null);
                selected = pieceSelected;
                scrollView.animate().translationY(0);
                arFragmentPlatos.getArSceneView().getPlaneRenderer().setVisible(true);
                selected = 0;
                setBackground(view.getId());
            }
        });
    }

    // Metodo que agrega el boton para ver la descripción del modelo
    public void addButtonInfo(AnchorNode anchorNode, TransformableNode model, Integer image, String txtTitulo, String txtFecha, String txtDim, String txtArtefacto, String txtTipo, String txtDescripcion){
        //Crear renderable
        ViewRenderable.builder()
                .setView(this, R.layout.info_model)
                .build()
                .thenAccept(viewRenderable -> {
                    TransformableNode nameView = new TransformableNode(arFragmentPlatos.getTransformationSystem());
                    nameView.setLocalPosition(new Vector3(0,0,model.getLocalPosition().z+0.35f));
                    //Rotacion
                    Quaternion localRotation = Quaternion.axisAngle( new Vector3(1.0f, 0.0f, 0f), -90f);
                    nameView.setLocalRotation(localRotation);
                    // cambiar nodo padre
                    nameView.setParent(anchorNode);
                    nameView.setRenderable(viewRenderable);
                    nameView.getRotationController().setEnabled(false);
                    nameView.getTranslationController().setEnabled(false);
                    nameView.getScaleController().setEnabled(false);

                    TextView txt_info = (TextView)viewRenderable.getView();

                    txt_info.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDescription(v, image, txtTitulo, txtFecha, txtDim, txtArtefacto, txtTipo, txtDescripcion);
                        }
                    });
                });
    }

    // Metodo que agrega el boton para ver la galeria de imagenes del modelo
    public void addGalleryInfo(AnchorNode anchorNode, TransformableNode model, Integer imgSuperior, Integer imgPrimarios, Integer imgAglomeracion){

        ViewRenderable.builder()
                .setView(this, R.layout.gallery_model)
                .build()
                .thenAccept(viewRenderable -> {
                    TransformableNode nameView = new TransformableNode(arFragmentPlatos.getTransformationSystem());
                    nameView.setLocalPosition(new Vector3(model.getLocalPosition().x-0.15f,0,model.getLocalPosition().z+0.31f));

                    Quaternion localRotation = Quaternion.axisAngle( new Vector3(1.0f, 0.0f, 0f), -90f);
                    nameView.setLocalRotation(localRotation);

                    nameView.setParent(anchorNode);
                    nameView.setRenderable(viewRenderable);
                    nameView.getRotationController().setEnabled(false);
                    nameView.getTranslationController().setEnabled(false);
                    nameView.getScaleController().setEnabled(false);

                    TextView txt_info = (TextView)viewRenderable.getView();

                    txt_info.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showGallery(v, imgSuperior, imgPrimarios, imgAglomeracion);
                        }
                    });
                });
    }

    // Metodo que agrega el boton para ver informacion en video del modelo
    public void addButtonVideo(AnchorNode anchorNode, TransformableNode model, Uri uriVideo){

        ViewRenderable.builder()
                .setView(this, R.layout.video_model)
                .build()
                .thenAccept(viewRenderable -> {
                    TransformableNode nameView = new TransformableNode(arFragmentPlatos.getTransformationSystem());
                    nameView.setLocalPosition(new Vector3(model.getLocalPosition().x+0.15f,0,model.getLocalPosition().z+0.31f));

                    Quaternion localRotation = Quaternion.axisAngle( new Vector3(1.0f, 0.0f, 0f), -90f);
                    nameView.setLocalRotation(localRotation);

                    nameView.setParent(anchorNode);
                    nameView.setRenderable(viewRenderable);
                    nameView.getRotationController().setEnabled(false);
                    nameView.getTranslationController().setEnabled(false);
                    nameView.getScaleController().setEnabled(false);

                    TextView txt_close = (TextView)viewRenderable.getView();

                    txt_close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showVideo(v, uriVideo);
                        }
                    });
                });
    }

    // Metodo que agrega los escuchas a cada una de las imagenes en el scroll de piezas
    private void setClickListener(){
        for (int i=0;i<arrayView.length;i++){
            arrayView[i].setOnClickListener(this);
        }
    }

    private android.graphics.Point getScreenCenter() {
        View vw = findViewById(R.id.contentArPlatos);
        return new android.graphics.Point(vw.getWidth()/2, vw.getHeight()/2);
    }

    private void addObject(){
        Frame frame = arFragmentPlatos.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    Anchor anchor = hit.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragmentPlatos.getArSceneView().getScene());
                    createModel(anchorNode, selected);
                    break;
                }
            }
        }
    }

    // Metodo que llama a setBackground para cambiar el color de fondo de las imagenes del scroll
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.pieza_2161) {
            selected = 1;
            setBackground(v.getId());
            addObject();
        }else if(v.getId() == R.id.pieza_2219) {
            selected = 2;
            setBackground(v.getId());
            addObject();
        }else if(v.getId() == R.id.pieza_2229) {
            selected = 3;
            setBackground(v.getId());
            addObject();
        }else if(v.getId() == R.id.pieza_3227) {
            selected = 4;
            setBackground(v.getId());
            addObject();
        }else if(v.getId() == R.id.pieza_3274) {
            selected = 5;
            setBackground(v.getId());
            addObject();
        }
    }

    // Metodo que cambia el background de la imagen seleccionada
    public void setBackground(int id){
        for (int i=0;i<arrayView.length;i++){
            if (arrayView[i].getId() == id)
                //arrayView[i].setBackgroundColor(Color.parseColor("#DDE7E3E3"));
                arrayView[i].setBackgroundColor(this.getColor(R.color.colorDark));
            else
                //arrayView[i].setBackgroundColor(Color.TRANSPARENT);
                arrayView[i].setBackgroundColor(this.getColor(R.color.white_dark));
        }
    }
}
