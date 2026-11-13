package com.olgun.kart;
import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.util.*;
import java.io.*;
import java.util.regex.*;

public class MainActivity extends Activity {
 LinearLayout root,items;
 LinkedHashMap<Integer,String> questions=new LinkedHashMap<>(),answers=new LinkedHashMap<>();
 int green=Color.rgb(220,252,231),dark=Color.rgb(22,101,52);
 GradientDrawable bg(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(22);d.setStroke(2,Color.rgb(134,239,172));return d;}
 TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(dark);t.setPadding(18,16,18,16);return t;}
 void addBtn(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setOnClickListener(v->r.run());root.addView(b);}
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(green);getWindow().setNavigationBarColor(green);
 ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(20,20,20,20);root.setBackgroundColor(Color.rgb(248,250,252));scroll.addView(root);setContentView(scroll);
 TextView title=text("Kart",29);title.setTypeface(null,Typeface.BOLD);title.setBackground(bg(green));root.addView(title);
 root.addView(text("Soru ve cevapları TXT veya galeriden resim olarak ayrı ayrı yükleyin. Numaralar eşleştiğinde karta dokunarak cevabı görün.",14));
 addBtn("Soruları yükle (TXT)",()->pick(101,"text/plain"));
 addBtn("Cevapları yükle (TXT)",()->pick(102,"text/plain"));
 addBtn("Soruları yükle (Resim)",()->pick(201,"image/*"));
 addBtn("Cevapları yükle (Resim)",()->pick(202,"image/*"));
 items=new LinearLayout(this);items.setOrientation(1);root.addView(items);
 android.content.SharedPreferences p=getSharedPreferences("kart",0);
 parseSaved(p.getString("q",""),questions);parseSaved(p.getString("a",""),answers);render();
 }
 void pick(int code,String type){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType(type);i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,code);}
 void parseSaved(String s,LinkedHashMap<Integer,String> target){target.clear();for(String row:s.split("\\n")){int split=row.indexOf('|');if(split>0)try{target.put(Integer.parseInt(row.substring(0,split)),row.substring(split+1));}catch(Exception ignored){}}}
 String serialize(LinkedHashMap<Integer,String> map){StringBuilder b=new StringBuilder();for(Map.Entry<Integer,String> e:map.entrySet())b.append(e.getKey()).append('|').append(e.getValue().replace("\n"," ")).append("\n");return b.toString();}
 void save(){getSharedPreferences("kart",0).edit().putString("q",serialize(questions)).putString("a",serialize(answers)).apply();}
 // Numbered entries are matched by their explicit number. Unnumbered text is kept in order.
 LinkedHashMap<Integer,String> parse(String raw){LinkedHashMap<Integer,String> map=new LinkedHashMap<>();int current=-1;int auto=1;
 Pattern numbered=Pattern.compile("^\\s*(?:Soru|Cevap|Question|Answer)?\\s*(\\d{1,4})\\s*[.)\\-:]\\s*(.*)$",Pattern.CASE_INSENSITIVE);
 for(String line:raw.split("\\r?\\n")){line=line.trim();if(line.isEmpty())continue;Matcher m=numbered.matcher(line);
 if(m.matches()){current=Integer.parseInt(m.group(1));map.put(current,m.group(2).trim());auto=Math.max(auto,current+1);}
 else if(current!=-1){map.put(current,map.get(current)+" "+line);}
 else{while(map.containsKey(auto))auto++;map.put(auto++,line);}
 }return map;}
 void ingest(String raw,boolean isQuestion){LinkedHashMap<Integer,String> parsed=parse(raw);if(parsed.isEmpty()){Toast.makeText(this,"Okunabilir içerik bulunamadı",Toast.LENGTH_LONG).show();return;}
 if(isQuestion){questions.clear();questions.putAll(parsed);}else{answers.clear();answers.putAll(parsed);}save();render();
 Toast.makeText(this,parsed.size()+" kayıt yüklendi",Toast.LENGTH_SHORT).show();}
 @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(res!=RESULT_OK||data==null)return;
 Uri uri=data.getData();if(uri==null)return;boolean isQuestion=req==101||req==201;
 if(req==201||req==202){try{
 InputImage img=InputImage.fromFilePath(this,uri);
 com.google.mlkit.vision.text.TextRecognizer recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
 recognizer.process(img).addOnSuccessListener(result->{ingest(result.getText(),isQuestion);recognizer.close();})
 .addOnFailureListener(e->{Toast.makeText(this,"Resim okunamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();recognizer.close();});
 }catch(Exception e){Toast.makeText(this,"Görsel açılamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();}return;}
 try(BufferedReader br=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)))){
 StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line).append("\n");ingest(sb.toString(),isQuestion);
 }catch(Exception e){Toast.makeText(this,"Dosya okunamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();}
 }
 void render(){items.removeAllViews();if(questions.isEmpty()){items.addView(text("Henüz soru yüklenmedi.",16));return;}
 for(Map.Entry<Integer,String> entry:questions.entrySet()){
 int number=entry.getKey();LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(8,8,8,8);card.setBackground(bg(green));
 LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,8,0,12);items.addView(card,lp);
 TextView q=text(number+". "+entry.getValue(),18);q.setTypeface(null,Typeface.BOLD);card.addView(q);
 String answer=answers.get(number);
 TextView a=text(answer!=null&&!answer.isEmpty()?answer:"Bu numaraya ait cevap bulunamadı.",16);
 a.setBackground(bg(Color.WHITE));a.setVisibility(View.GONE);card.addView(a);
 card.setOnClickListener(v->a.setVisibility(a.getVisibility()==View.GONE?View.VISIBLE:View.GONE));
 }
 }
}