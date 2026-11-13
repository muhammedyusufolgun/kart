package com.olgun.kart;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.Color;import android.graphics.Typeface;import android.graphics.drawable.GradientDrawable;import android.view.*;import android.widget.*;import java.util.*;import java.io.*;
public class MainActivity extends Activity {
 LinearLayout root,items; ArrayList<String> questions=new ArrayList<>(),answers=new ArrayList<>(); int green=Color.rgb(220,252,231); int dark=Color.rgb(22,101,52);
 GradientDrawable bg(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(22);d.setStroke(2,Color.rgb(134,239,172));return d;}
 TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(dark);t.setPadding(18,16,18,16);return t;}
 void addBtn(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setOnClickListener(v->r.run());root.addView(b);}
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(green);getWindow().setNavigationBarColor(green);
 ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(20,20,20,20);root.setBackgroundColor(Color.rgb(248,250,252));scroll.addView(root);setContentView(scroll);
 TextView title=text("Kart",29);title.setTypeface(null,Typeface.BOLD);title.setBackground(bg(green));root.addView(title);
 TextView hint=text("Sorular ve cevaplar ayrı metin dosyalarından yüklenir. Her satır bir karttır. Karta dokunarak cevabı açıp kapatın.",14);root.addView(hint);
 addBtn("Soruları yükle (TXT)",()->pick(101));addBtn("Cevapları yükle (TXT)",()->pick(102));
 items=new LinearLayout(this);items.setOrientation(1);root.addView(items);
 android.content.SharedPreferences p=getSharedPreferences("kart",0);questions.addAll(Arrays.asList(p.getString("q","").split("\\n",-1)));answers.addAll(Arrays.asList(p.getString("a","").split("\\n",-1)));if(questions.size()==1&&questions.get(0).isEmpty())questions.clear();if(answers.size()==1&&answers.get(0).isEmpty())answers.clear();render();
 }
 void pick(int code){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("text/plain");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,code);}
 @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(res!=RESULT_OK||data==null)return;try{BufferedReader br=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(data.getData())));ArrayList<String> lines=new ArrayList<>();String line;while((line=br.readLine())!=null){if(!line.trim().isEmpty())lines.add(line.trim());}if(req==101){questions.clear();questions.addAll(lines);}else{answers.clear();answers.addAll(lines);}getSharedPreferences("kart",0).edit().putString("q",android.text.TextUtils.join("\n",questions)).putString("a",android.text.TextUtils.join("\n",answers)).apply();render();}catch(Exception e){Toast.makeText(this,"Dosya okunamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
 void render(){items.removeAllViews();for(int i=0;i<questions.size();i++){final int index=i;LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setBackground(bg(green));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,8,0,12);items.addView(card,lp);
 TextView q=text((i+1)+". "+questions.get(i),18);q.setTypeface(null,Typeface.BOLD);card.addView(q);
 TextView a=text(i<answers.size()?answers.get(i):"Eşleşen cevap yüklenmedi",16);a.setBackground(bg(Color.WHITE));a.setVisibility(View.GONE);card.addView(a);
 card.setOnClickListener(v->a.setVisibility(a.getVisibility()==View.GONE?View.VISIBLE:View.GONE));}}
}