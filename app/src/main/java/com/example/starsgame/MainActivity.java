package com.example.starsgame;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ImageView imageViewStar;
    //Создаем переменную для ссылки
    // footbal private String url = "https://www.transfermarkt.ru/spieler-statistik/wertvollstespieler/marktwertetop";
    private String url = "https://www.forbes.ru/rating/403469-40-samyh-uspeshnyh-zvezd-rossii-do-40-let-reyting-forbes";
    //Два массива (фото и имена)
    private ArrayList<String> urls;
    private ArrayList<String> names;
    //Добавим все кнопки в один массив (после создания метода generateQuestion, где получаем номер вопроса рандомно)
    private ArrayList<Button> buttons;
    private ArrayList<Integer> nameIndex;
    //Две переменные для метода generateQuestion
    private int numberOfImage;
    private int numberOfRightButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageViewStar = findViewById(R.id.imageViewStar);
        //определяем переменным два новых массива.
        urls = new ArrayList<>();
        names = new ArrayList<>();
        buttons = new ArrayList<>();
        nameIndex = new ArrayList<>();
        //Добавим все наши кнопки в массив
        buttons.add(findViewById(R.id.button0));
        buttons.add(findViewById(R.id.button1));
        buttons.add(findViewById(R.id.button2));
        buttons.add(findViewById(R.id.button3));
        for (int i = 0; i < buttons.size(); i++)
            nameIndex.add(i);
        getContent();
        playGame();
    }

    private void getContent() {
        DownloadContentTask task = new DownloadContentTask();
        try {
            String content = task.execute(url).get();
            // footbal String start = "<table class=\"items\">";
            // footbal String finish = "<div class=\"pager\">";
            String start = "<div class=\"items\">";
            String finish = "<div class=\"panel-pane pane-rating-content\">";
            Pattern pattern = Pattern.compile(start + "(.*?)" + finish);
            Matcher matcher = pattern.matcher(content);
            //создадим переменную с обрезанным контентом
            String splitContent = "";
            while (matcher.find()) {
                splitContent = matcher.group(1);
            }

            //Все работает, указанный нами контент вынимается в виде одной длинной строки, теперь по очередно вынимаем данные в виде массива фото и массива имен.
            //Для этого нам понадобятся два паттерна

            // footballImg  Pattern patternImg = Pattern.compile("#\"><img src=\"(.*?)\" title=");
            // footballCoast  Pattern patternName = Pattern.compile("<span title=\"(.*?)\"");
            // footballName Pattern patternName = Pattern.compile("<a title=\"(.*?)\" class");

            Pattern patternImg = Pattern.compile("<img src=\"(.*?)\"");
            Pattern patternName = Pattern.compile("alt=\"(.*?)\"/>");
            Matcher matcherImg = patternImg.matcher(splitContent);
            Matcher matcherName = patternName.matcher(splitContent);
            while (matcherImg.find()) {
                urls.add(matcherImg.group(1));
            }
            while (matcherName.find()) {
                names.add(matcherName.group(1));
            }
            //проверка как все выводится
            //for (String s: names) {
            //Log.i("MyResult", s);
            //}
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void playGame() {
        //вызвали метод генерации вопросов
        numberOfRightButton = (int) (Math.random() * buttons.size());
        numberOfImage = (int) (Math.random() * names.size());
        DownloadImageTask task = new DownloadImageTask();
        try {
            int countOfNames = names.size();
            Bitmap bitmap = task.execute(urls.get(numberOfImage)).get();
            if (bitmap != null) {
                imageViewStar.setImageBitmap(bitmap);

                for (int i = 0; i < buttons.size(); i++) {
                    int answer;

                    if (i == numberOfRightButton) {
                        answer = numberOfImage;
                    } else {
                        do {
                            answer = (int) (Math.random() * names.size());
                            //он не должен быть верным
                            if (answer == numberOfImage) continue;
                            //он не должен совпадать с предыдущим значение
                            int flag = 0;
                            for (int j = 0; j < i; j++) {
                                if (answer == nameIndex.get(j)) {
                                    flag = 1;
                                }
                            }
                            //если мы не попали на совпадение, то тогда выходим из цикла
                            if (flag == 0) break;
                        } while (true);
                    }
                    nameIndex.set(i, answer);
                    buttons.get(i).setText(names.get(answer));
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void onClickAnswer(View view) {
        Button button = (Button) view;
        String tag = button.getTag().toString();
        if (Integer.parseInt(tag) == numberOfRightButton) {
            Toast.makeText(this, "Верно", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Неверно, правильный ответ: " + names.get(numberOfImage), Toast.LENGTH_SHORT).show();
        }
        playGame();
    }

    public static class DownloadContentTask extends AsyncTask<String, View, String> {

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder result = new StringBuilder();
            URL url = null;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    result.append(line);
                    line = bufferedReader.readLine();
                }
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }

    public static class DownloadImageTask extends AsyncTask<String, View, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }
}
