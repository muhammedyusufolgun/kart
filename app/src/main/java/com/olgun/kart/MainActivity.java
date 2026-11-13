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
import org.json.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class MainActivity extends Activity {
 LinearLayout root,items,library; 
 LinkedHashMap<Integer,String> questions=new LinkedHashMap<>(),answers=new LinkedHashMap<>();
 ArrayList<JSONObject> saved=new ArrayList<>(); int green=Color.rgb(220,252,231),dark=Color.rgb(22,101,52);
 int chosenCount=0; boolean practice=false; String activeName=""; int selectedRecord=-1;
 int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);}
 GradientDrawable bg(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(12));d.setStroke(dp(1),Color.rgb(134,239,172));return d;}
 TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(dark);t.setPadding(dp(12),dp(12),dp(12),dp(12));return t;}
 void button(LinearLayout parent,String s,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setOnClickListener(v->r.run());parent.addView(b);}
 void colored(LinearLayout parent,String label,int color,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(12);b.setTextColor(Color.rgb(31,41,55));b.setBackground(bg(color));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(54),1);p.setMargins(dp(3),dp(3),dp(3),dp(3));parent.addView(b,p);b.setOnClickListener(v->action.run());}
 void pair(String left,String right,int lc,int rc,Runnable la,Runnable ra){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);root.addView(row);colored(row,left,lc,la);colored(row,right,rc,ra);}
 void heading(LinearLayout parent,String s){TextView t=text(s,21);t.setTypeface(null,Typeface.BOLD);parent.addView(t);}
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(Color.rgb(248,250,252));getWindow().setNavigationBarColor(Color.rgb(248,250,252));
 LinearLayout frame=new LinearLayout(this);frame.setOrientation(1);frame.setBackgroundColor(Color.rgb(248,250,252));setContentView(frame);
 ScrollView scroll=new ScrollView(this);frame.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
 root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(16),dp(42),dp(16),dp(8));scroll.addView(root);
 TextView title=text("Kart-Eşleşme",27);title.setTypeface(null,Typeface.BOLD);title.setGravity(Gravity.CENTER);title.setTextColor(Color.WHITE);title.setBackground(bg(Color.rgb(20,83,45)));title.setOnClickListener(v->resetHome());root.addView(title);
 root.addView(text("Soruları ve cevapları ayrı yükleyin. Numaralı sorular aynı numaralı cevaplarla eşleştirilir.",14));
 LinearLayout importRow=new LinearLayout(this);importRow.setOrientation(LinearLayout.HORIZONTAL);importRow.setWeightSum(4f);root.addView(importRow);
 colored(importRow,"Soru\nTXT",Color.rgb(254,240,138),()->pick(101,"text/plain"));
 colored(importRow,"Cevap\nTXT",Color.rgb(254,240,138),()->pick(102,"text/plain"));
 colored(importRow,"Soru\nResim",green,()->pick(201,"image/*"));
 colored(importRow,"Cevap\nResim",green,()->pick(202,"image/*"));
 LinearLayout saveRow=new LinearLayout(this);saveRow.setOrientation(LinearLayout.HORIZONTAL);root.addView(saveRow);colored(saveRow,"Eşleşmeyi İsimle Kaydet",Color.rgb(186,230,253),this::savePrompt);
 heading(root,"Kayıtlı Eşleşmeler");library=new LinearLayout(this);library.setOrientation(1);root.addView(library);
 heading(root,"Kartlarla Tekrar");
 LinearLayout startRow=new LinearLayout(this);root.addView(startRow);
 colored(startRow,"Kart Sayısı Seç ve Başlat",Color.rgb(147,197,253),this::choosePractice);
 items=new LinearLayout(this);items.setOrientation(1);root.addView(items);
 TextView footer=text("Muhammed Yusuf OLGUN",13);footer.setTextColor(Color.DKGRAY);footer.setGravity(Gravity.CENTER);footer.setPadding(dp(8),dp(10),dp(8),dp(56));frame.addView(footer);
 load();showLibrary();render();
 }
 void resetHome(){practice=false;chosenCount=0;questions.clear();answers.clear();activeName="";selectedRecord=-1;render();showLibrary();Toast.makeText(this,"Başlangıç ekranına dönüldü",Toast.LENGTH_SHORT).show();}
 void pick(int code,String type){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType(type);i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,code);}
 LinkedHashMap<Integer,String> parse(String raw){
 LinkedHashMap<Integer,String> map=new LinkedHashMap<>();int current=-1,auto=1;
 Pattern p=Pattern.compile("^\\s*(?:(?:Soru|Cevap|Question|Answer)\\s*)?(\\d{1,4})\\s*[.)\\-:]\\s*(.*)$",Pattern.CASE_INSENSITIVE);
 for(String line:raw.split("\\r?\\n")){line=line.trim();if(line.isEmpty())continue;Matcher m=p.matcher(line);
 if(m.matches()){current=Integer.parseInt(m.group(1));map.put(current,m.group(2).trim());auto=Math.max(auto,current+1);}
 else if(current!=-1)map.put(current,map.get(current)+" "+line);
 else{while(map.containsKey(auto))auto++;map.put(auto++,line);}
 }return map;
 }
 JSONObject mapJson(LinkedHashMap<Integer,String> map)throws JSONException{JSONObject o=new JSONObject();for(Map.Entry<Integer,String> e:map.entrySet())o.put(String.valueOf(e.getKey()),e.getValue());return o;}
 void fromJson(JSONObject o,LinkedHashMap<Integer,String> map){map.clear();if(o==null)return;ArrayList<Integer> keys=new ArrayList<>();Iterator<String> it=o.keys();while(it.hasNext())try{keys.add(Integer.parseInt(it.next()));}catch(Exception ignored){}Collections.sort(keys);for(int n:keys)map.put(n,o.optString(String.valueOf(n)));}
 void persist(){JSONArray a=new JSONArray();for(JSONObject o:saved)a.put(o);getSharedPreferences("kart",0).edit().putString("saved",a.toString()).apply();}
 void load(){try{JSONArray a=new JSONArray(getSharedPreferences("kart",0).getString("saved","[]"));for(int i=0;i<a.length();i++)saved.add(a.getJSONObject(i));}catch(Exception ignored){}}
 void ingest(String raw,boolean isQuestion){LinkedHashMap<Integer,String> m=parse(raw);if(m.isEmpty()){Toast.makeText(this,"Okunabilir içerik bulunamadı",Toast.LENGTH_LONG).show();return;}
 if(isQuestion){questions.clear();questions.putAll(m);}else{answers.clear();answers.putAll(m);}practice=false;render();
 Toast.makeText(this,m.size()+" kayıt okundu",Toast.LENGTH_SHORT).show();}
 @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(res!=RESULT_OK||data==null||data.getData()==null)return;
 Uri uri=data.getData();boolean isQuestion=req==101||req==201;
 if(req==201||req==202){try{
 InputImage img=InputImage.fromFilePath(this,uri);
 com.google.mlkit.vision.text.TextRecognizer rec=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
 rec.process(img).addOnSuccessListener(result->{ingest(result.getText(),isQuestion);rec.close();})
 .addOnFailureListener(e->{Toast.makeText(this,"Görsel okunamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();rec.close();});
 }catch(Exception e){Toast.makeText(this,"Görsel açılamadı",Toast.LENGTH_LONG).show();}return;}
 try(BufferedReader br=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)))){
 StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line).append("\n");ingest(sb.toString(),isQuestion);
 }catch(Exception e){Toast.makeText(this,"Dosya okunamadı",Toast.LENGTH_LONG).show();}
 }
 void savePrompt(){if(questions.isEmpty()){Toast.makeText(this,"Önce soruları yükleyin",Toast.LENGTH_SHORT).show();return;}
 EditText input=new EditText(this);input.setSingleLine(true);input.setHint("Örnek: Tarih 1. Ünite");
 new AlertDialog.Builder(this).setTitle("Eşleşmeye isim ver").setView(input).setNegativeButton("İptal",null)
 .setPositiveButton("Kaydet",(d,w)->{String name=input.getText().toString().trim();if(name.isEmpty()){Toast.makeText(this,"İsim boş olamaz",Toast.LENGTH_SHORT).show();return;}
 try{JSONObject record=new JSONObject();record.put("name",name);record.put("questions",mapJson(questions));record.put("answers",mapJson(answers));record.put("created",System.currentTimeMillis());saved.add(record);activeName=name;selectedRecord=saved.size()-1;persist();showLibrary();}catch(Exception ignored){}}).show();
 }
 void showLibrary(){
 library.removeAllViews();
 if(saved.isEmpty()){selectedRecord=-1;library.addView(text("Henüz kayıtlı eşleşme yok.",14));return;}
 if(selectedRecord>=saved.size())selectedRecord=-1;
 RadioGroup group=new RadioGroup(this);group.setOrientation(LinearLayout.VERTICAL);library.addView(group);
 for(int i=0;i<saved.size();i++){
  JSONObject o=saved.get(i);String name=o.optString("name","İsimsiz");
  JSONObject q=o.optJSONObject("questions");int count=q==null?0:q.length();
  RadioButton item=new RadioButton(this);item.setId(20000+i);
  item.setText(name+" ("+count+" kart)");item.setTextSize(17);item.setTextColor(dark);
  item.setPadding(dp(10),dp(10),dp(10),dp(10));item.setBackground(bg(Color.WHITE));
  RadioGroup.LayoutParams lp=new RadioGroup.LayoutParams(-1,-2);lp.setMargins(0,dp(3),0,dp(3));group.addView(item,lp);
  if(i==selectedRecord)item.setChecked(true);
 }
 group.setOnCheckedChangeListener((g,id)->selectedRecord=id-20000);
 LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);library.addView(actions);
 colored(actions,"Seç ve Kullan",green,()->{
  if(selectedRecord<0||selectedRecord>=saved.size()){Toast.makeText(this,"Önce listeden bir eşleşme seçin",Toast.LENGTH_SHORT).show();return;}
  JSONObject o=saved.get(selectedRecord);String name=o.optString("name","İsimsiz");
  fromJson(o.optJSONObject("questions"),questions);fromJson(o.optJSONObject("answers"),answers);
  activeName=name;practice=false;render();Toast.makeText(this,name+" açıldı",Toast.LENGTH_SHORT).show();
 });
 colored(actions,"Kaydı Sil",Color.rgb(254,202,202),()->{
  if(selectedRecord<0||selectedRecord>=saved.size()){Toast.makeText(this,"Önce silinecek kaydı seçin",Toast.LENGTH_SHORT).show();return;}
  confirmDelete(selectedRecord,saved.get(selectedRecord).optString("name","İsimsiz"));
 });
}
 void confirmDelete(int index,String name){
 new AlertDialog.Builder(this).setTitle("Silme onayı 1/2").setMessage(name+" kaydını silmek istiyor musunuz?")
 .setNegativeButton("Vazgeç",null).setPositiveButton("Devam", (d,w)->
 new AlertDialog.Builder(this).setTitle("Silme onayı 2/2").setMessage("Bu işlem geri alınamaz. '"+name+"' kalıcı silinsin mi?")
 .setNegativeButton("Hayır",null).setPositiveButton("Evet, sil",(d2,w2)->{if(index>=0&&index<saved.size()){saved.remove(index);selectedRecord=-1;persist();showLibrary();}}).show()).show();
 }
 void choosePractice(){if(questions.isEmpty()){Toast.makeText(this,"Önce soru yükleyin veya kayıt seçin",Toast.LENGTH_SHORT).show();return;}
 NumberPicker picker=new NumberPicker(this);picker.setMinValue(1);picker.setMaxValue(questions.size());picker.setValue(questions.size());
 new AlertDialog.Builder(this).setTitle("Tekrar edilecek kart sayısı").setView(picker).setNegativeButton("İptal",null)
 .setPositiveButton("Başlat",(d,w)->{chosenCount=picker.getValue();practice=true;render();}).show();
 }
 void render(){items.removeAllViews();if(questions.isEmpty()){items.addView(text("Henüz soru yüklenmedi.",16));return;}
 if(!activeName.isEmpty())items.addView(text("Seçili kayıt: "+activeName,15));
 int shown=0;for(Map.Entry<Integer,String> e:questions.entrySet()){if(practice&&shown>=chosenCount)break;shown++;int n=e.getKey();
 LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setGravity(Gravity.CENTER);card.setPadding(dp(12),dp(12),dp(12),dp(12));card.setMinimumHeight(getResources().getDisplayMetrics().widthPixels-dp(40));card.setBackground(bg(green));
 LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(8),0,dp(12));items.addView(card,lp);
 TextView q=text(n+". "+e.getValue(),20);q.setTypeface(null,Typeface.BOLD);q.setGravity(Gravity.CENTER);card.addView(q);
 String a=answers.get(n);TextView answer=text(a==null||a.isEmpty()?"Bu numaraya ait cevap bulunamadı":a,18);answer.setGravity(Gravity.CENTER);answer.setBackground(bg(Color.WHITE));answer.setVisibility(View.GONE);card.addView(answer);
 card.setOnClickListener(v->answer.setVisibility(answer.getVisibility()==View.GONE?View.VISIBLE:View.GONE));
 }}
}