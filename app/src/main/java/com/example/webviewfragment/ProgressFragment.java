package com.example.webviewfragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ProgressFragment extends Fragment {
    TextView contentView; //здесь будет текстовая информация с интернет ресурса
    String contentText = null; //здесь будет хранится текст с интернет ресурса
    WebView webView; //здесь будет отображена страница интернет ресурса

    //происходит создание фрагмента, метод вызывается после вызова соответсвующего метода у activity
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); //включаем сохранение состяния фрагмента вне зависимости от ориентации экрана
    }

    //фрагмент создает визуальный интерфейс
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false); //inflater создает view из файла разметки фрагмента
        contentView = (TextView) view.findViewById(R.id.content);
        webView = (WebView) view.findViewById(R.id.webView);

        // если данные ранее были загружены
        if(contentText!=null){
            contentView.setText(contentText);
            webView.loadData(contentText, "text/html; charset=utf-8", "utf-8");
        }

        Button btnFetch = (Button)view.findViewById(R.id.downloadBtn);
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(contentText==null){
                    contentView.setText("Загрузка...");
                    /*
                    создаем безыимянный экземпляр класса-наследнника AsyncTask
                    вызываем метод execute для начала работы и
                    одновременно передаем строку с адресом сайта
                     */
                    new ProgressTask().execute("https://developer.android.com/index.html"); //
                }
            }
        });
        return view; //onCreateView должен вернуть визуальное представление т.е. view
    }
    //класс работающий в отдельном потоке
    //типизированные параметры String - хранение информации для выполнения задачи (передали адрес сайта)
    //Void - тип обьектов для индикации процесса выполнения задачи (никак не сигнализируем)
    //String - тип результата задачи (вернули текстувую информацию с сайта)
    private class ProgressTask extends AsyncTask<String, Void, String> {
       //метод работает в фоновом потоке
        @Override
        protected String doInBackground(String... path) {

            String content;
            try{
                //метод загружает данные со страницы
                //в параметрах строка с адресом
                content = getContent(path[0]);
            }
            catch (IOException ex){
                content = ex.getMessage();
            }

            return content; //вернули текст загруженный с сайта
        }
        /*
        после завершения метода doInBackground из главного
        потока вызывается onPostExecute

         */
        @Override
        protected void onPostExecute(String content) {

            contentText=content; //перемещаем текст с сайта в переменную
            contentView.setText(content); //текстовому полю даем текст с сайта
            //c помощью webView представляем текст с сайта в виде html страницы
            webView.loadData(content, "text/html; charset=utf-8", "utf-8");
            Toast.makeText(getActivity(), "Данные загружены", Toast.LENGTH_SHORT)
                    .show();
        }
        //непосредственно метод загружающий с сайта информацию
        private String getContent(String path) throws IOException { //
            BufferedReader reader=null; //класс считывает в буфер текст из потока, в параметры принимает входной поток
            try {
                URL url=new URL(path); //передаем адрес сайта
                HttpsURLConnection c=(HttpsURLConnection)url.openConnection(); //открываем соединение с нашим адресом
                c.setRequestMethod("GET"); //установка метода получения данных - GET
                c.setReadTimeout(10000); //установка таймаута перед выполнением - 10000 миллисекунд
                c.connect(); //подключаемся к ресурсу
                reader= new BufferedReader(new InputStreamReader(c.getInputStream())); //получаем входной поток
                StringBuilder buf=new StringBuilder(); //создаем строковый буфер
                String line=null;
                while ((line=reader.readLine()) != null) { //с потока буфера читаем построчно и перемещаем
                    buf.append(line + "\n"); //в строковый буфер
                }
                return(buf.toString()); //возращаем строку
            }
            finally {
                if (reader != null) { //закрываем поток, если он пустой
                    reader.close();
                }
            }
        }
    }
}