package com.univalle.appmuseo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.univalle.appmuseo.QuizContract.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyQuiz.db";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase db;

    public QuizDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;

        final String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " +
                QuestionsTable.TABLE_NAME + " ( " +
                QuestionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QuestionsTable.COLUMN_QUESTION + " TEXT, " +
                QuestionsTable.COLUMN_OPTION1 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION2 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION3 + " TEXT, " +
                QuestionsTable.COLUMN_ANSWER_NR + " INTEGER" +
                ")";
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);
        fillQuestionsTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.TABLE_NAME);
        onCreate(db);
    }

    private void fillQuestionsTable() {
        Question q1 = new Question("¿De que fecha datan la mayoría de piezas de la exposición?", "a) 1250 -1500 d.C", "b) 1500 - 1800 d.C", "c) 1600 - 1800 d.C", 1);
        addQuestion(q1);

        Question q2 = new Question("¿Que representa la estrella de 8 puntas según la cultura Tuza?", "a) La Luna", "b) El Sol", "c) Las Estrellas", 2);
        addQuestion(q2);

        Question q3 = new Question("¿Algunas imágenes de individuos en los platos Tuza llevan un tocado sobres sus cabezas, por lo cual podrían ser?", "a) Guerreros", "b) Curanderos", "c) Chamanes", 3);
        addQuestion(q3);

        Question q4 = new Question("¿Las lineas entrecruzadas que representan para las comunidades Pastos?", "a) La chagra", "b) El sol", "c) La madre tierra", 1);
        addQuestion(q4);

        Question q5 = new Question("¿Según la cultura Tuza cuales son los colores de la vida y la muerte?", "a) Rojo y negro", "b) Azul y negro", "c) Verde y rojo", 1);
        addQuestion(q5);

        Question q6 = new Question("¿En las ocarinas vistas en la exposición, cual de los siguientes animales representativos aparece?", "a) Mono", "b) Garza", "c) Curie", 3);
        addQuestion(q6);

        Question q7 = new Question("¿En los platos vistos en la exposición, cual de los siguientes animales representativos aparece?", "a) Serpiente", "b) Venado", "c) Mono", 2);
        addQuestion(q7);

        Question q8 = new Question("¿Cual de los siguientes grupos de colores predomina en la ceramica Tuza vista en la exposiciones?", "a) Crema, rojo y verde", "b) Crema, rojo y negro", "c) Crema, negro y blanco", 2);
        addQuestion(q8);
    }

    private void addQuestion(Question question) {
        ContentValues cv = new ContentValues();
        cv.put(QuestionsTable.COLUMN_QUESTION, question.getQuestion());
        cv.put(QuestionsTable.COLUMN_OPTION1, question.getOption1());
        cv.put(QuestionsTable.COLUMN_OPTION2, question.getOption2());
        cv.put(QuestionsTable.COLUMN_OPTION3, question.getOption3());
        cv.put(QuestionsTable.COLUMN_ANSWER_NR, question.getAnswerNr());
        db.insert(QuestionsTable.TABLE_NAME, null, cv);
    }

    public List<Question> getAllQuestions() {
        List<Question> questionList = new ArrayList<>();
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + QuestionsTable.TABLE_NAME, null);

        if (c.moveToFirst()) {
            do {
                Question question = new Question();
                question.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                question.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                question.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                question.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                question.setAnswerNr(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_NR)));
                questionList.add(question);
            } while (c.moveToNext());
        }

        c.close();
        return questionList;
    }
}
