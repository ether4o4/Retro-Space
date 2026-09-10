package com.ether404.retrospace;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import androidx.core.content.FileProvider;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.*;

public class MainActivity extends Activity {
    static final int PICK=10,CAMERA=11,EXPORT=12;
    File root; Desktop desktop; WebView browserView; Uri cameraUri;
    @Override public void onCreate(Bundle b){super.onCreate(b);if(Build.VERSION.SDK_INT>=28)WebView.setDataDirectorySuffix("retrospace");root=new File(getFilesDir(),"workspace");init();desktop=new Desktop();setContentView(desktop);}
    void init(){for(String s:new String[]{"Desktop","Documents","Downloads","Pictures","Projects","Browser","Terminal"})new File(root,s).mkdirs();}
    void home(){browserView=null;setContentView(desktop);}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);startActivityForResult(i,PICK);}
    void copy(Uri u)throws Exception{String n=u.getLastPathSegment();if(n==null||n.contains("/"))n="imported_"+System.currentTimeMillis();File o=new File(new File(root,"Downloads"),n);InputStream in=getContentResolver().openInputStream(u);FileOutputStream out=new FileOutputStream(o);byte[] b=new byte[8192];int z;while((z=in.read(b))>0)out.write(b,0,z);in.close();out.close();}
    void camera(){if(Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA);return;}try{File f=new File(new File(root,"Pictures"),"IMG_"+new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date())+".jpg");cameraUri=FileProvider.getUriForFile(this,"com.ether404.retrospace.files",f);Intent i=new Intent("android.media.action.IMAGE_CAPTURE");i.putExtra("output",cameraUri);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);startActivityForResult(i,CAMERA);}catch(Exception e){toast(e.getMessage());}}
    void browser(){browserView=new WebView(this);browserView.setBackgroundColor(Color.WHITE);WebSettings s=browserView.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setSupportZoom(true);browserView.setWebViewClient(new WebViewClient());browserView.loadUrl("https://www.google.com");setContentView(browserView);}
    void files(String dir){LinearLayout l=bar(dir);File[] fs=new File(root,dir).listFiles();if(fs!=null)for(File f:fs){TextView t=new TextView(this);t.setText("  "+f.getName()+"   "+f.length()+" bytes");t.setTextColor(Color.BLACK);t.setTextSize(14);t.setPadding(8,14,8,14);l.addView(t);}setContentView(l);}
    LinearLayout bar(String title){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);TextView b=new TextView(this);b.setText("  "+title+"                                      BACK");b.setTextColor(Color.WHITE);b.setTextSize(15);b.setGravity(Gravity.CENTER_VERTICAL);b.setBackgroundColor(Color.rgb(0,0,128));b.setOnClickListener(v->home());l.addView(b,new LinearLayout.LayoutParams(-1,52));return l;}
    void terminal(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setBackgroundColor(Color.BLACK);TextView out=new TextView(this);out.setTextColor(Color.LTGRAY);out.setTypeface(Typeface.MONOSPACE);out.setText("Retro-Space terminal\nPrivate root: "+root+"\nType help\n\n$ ");out.setPadding(10,10,10,10);ScrollView sc=new ScrollView(this);sc.addView(out);l.addView(sc,new LinearLayout.LayoutParams(-1,0,1));EditText in=new EditText(this);in.setTextColor(Color.WHITE);in.setHintTextColor(Color.GRAY);in.setSingleLine(true);in.setTypeface(Typeface.MONOSPACE);in.setHint("command");l.addView(in,new LinearLayout.LayoutParams(-1,56));in.setOnEditorActionListener((v,a,e)->{String c=in.getText().toString().trim();if(!c.isEmpty()){out.append(c+"\n"+cmd(c)+"\n$ ");sc.post(()->sc.fullScroll(View.FOCUS_DOWN));in.setText("");}return true;});setContentView(l);in.requestFocus();}
    String cmd(String c){try{if(c.equals("help"))return "help  ls  pwd  find  cat  mkdir  rm  echo  date";if(c.equals("pwd"))return root.getAbsolutePath();if(c.equals("ls"))return join(root.list());if(c.equals("date"))return new Date().toString();if(c.startsWith("echo "))return c.substring(5);if(c.startsWith("mkdir ")){new File(root,c.substring(6).trim()).mkdirs();return "";}if(c.startsWith("cat ")){File f=safe(c.substring(4).trim());return f==null?"denied":read(f);}if(c.equals("find"))return tree(root,"");if(c.startsWith("rm ")){File f=safe(c.substring(3).trim());return f!=null&&del(f)?"removed":"failed";}Process p=new ProcessBuilder("/system/bin/sh","-c",c).directory(root).redirectErrorStream(true).start();ByteArrayOutputStream b=new ByteArrayOutputStream();InputStream i=p.getInputStream();byte[] x=new byte[4096];int n;while((n=i.read(x))>0)b.write(x,0,n);p.waitFor();return b.toString().trim();}catch(Exception e){return "error: "+e.getMessage();}}
    File safe(String p)throws Exception{File f=new File(root,p);return f.getCanonicalPath().startsWith(root.getCanonicalPath())?f:null;}
    String read(File f)throws Exception{if(!f.isFile())return "not a file";if(f.length()>1048576)return "file too large";return new String(java.nio.file.Files.readAllBytes(f.toPath()));}
    String join(String[] a){return a==null?"":String.join("\n",a);}
    String tree(File d,String p){StringBuilder s=new StringBuilder();File[] a=d.listFiles();if(a!=null)for(File f:a){s.append(p).append(f.getName()).append(f.isDirectory()?"/":"").append('\n');if(f.isDirectory())s.append(tree(f,p+f.getName()+"/"));}return s.toString();}
    boolean del(File f){if(f.isDirectory()){File[] a=f.listFiles();if(a!=null)for(File x:a)del(x);}return f.delete();}
    void export(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/zip");i.putExtra(Intent.EXTRA_TITLE,"Retro-Space-workspace.zip");startActivityForResult(i,EXPORT);}
    void zip(File src,ZipOutputStream z,String base)throws Exception{File[] fs=src.listFiles();if(fs==null)return;for(File f:fs){String n=base+f.getName()+(f.isDirectory()?"/":"");z.putNextEntry(new ZipEntry(n));if(f.isFile()){FileInputStream in=new FileInputStream(f);byte[] b=new byte[8192];int q;while((q=in.read(b))>0)z.write(b,0,q);in.close();}z.closeEntry();if(f.isDirectory())zip(f,z,n);}}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);try{if(r==PICK&&c==RESULT_OK&&d!=null){if(d.getClipData()!=null)for(int i=0;i<d.getClipData().getItemCount();i++)copy(d.getClipData().getItemAt(i).getUri());else copy(d.getData());toast("Imported into private workspace");}else if(r==EXPORT&&c==RESULT_OK&&d!=null){OutputStream o=getContentResolver().openOutputStream(d.getData());ZipOutputStream z=new ZipOutputStream(o);zip(root,z,"");z.finish();z.close();toast("Workspace exported");}else if(r==CAMERA&&c==RESULT_OK)toast("Photo saved inside Retro-Space/Pictures");}catch(Exception e){toast(e.getMessage());}}
    @Override public void onBackPressed(){if(browserView!=null&&browserView.canGoBack()){browserView.goBack();return;}if(getWindow().getDecorView().getRootView()!=desktop)home();else super.onBackPressed();}
    class Desktop extends View{Paint p=new Paint(3),t=new Paint(3);String[] names={"My Computer","File Explorer","Browser","Terminal","Camera","Import","Export","Settings","Documents","Downloads","Pictures","Projects"};Desktop(){super(MainActivity.this);t.setTypeface(Typeface.DEFAULT_BOLD);setFocusable(true);}
      protected void onDraw(Canvas c){c.drawColor(Color.rgb(0,128,128));p.setColor(Color.rgb(192,192,192));c.drawRect(0,0,getWidth(),34,p);p.setColor(Color.rgb(0,0,128));c.drawRect(2,2,getWidth()-2,31,p);t.setColor(Color.WHITE);t.setTextSize(16);c.drawText("Program Manager",10,23,t);for(int i=0;i<names.length;i++){int row=i<8?0:1,col=i<8?i:i-8;icon(c,18+col*92,54+row*110,names[i]);}t.setColor(Color.WHITE);t.setTextSize(11);c.drawText("PRIVATE WORKSPACE — data stays here until you explicitly import or export it.",18,300,t);}
      void icon(Canvas c,int x,int y,String s){p.setColor(Color.rgb(192,192,192));c.drawRect(x,y,x+54,y+54,p);p.setColor(Color.BLACK);p.setStyle(Paint.Style.STROKE);c.drawRect(x+3,y+3,x+51,y+51,p);p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(0,0,128));c.drawRect(x+12,y+10,x+42,y+36,p);t.setColor(Color.WHITE);t.setTextSize(8);float w=t.measureText(s);c.drawText(s,x+27-w/2,y+70,t);}
      public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;int row=e.getY()<135?0:1,col=Math.max(0,(int)((e.getX()-18)/92));int i=row==0?col:col+8;switch(i){case 1:files("Downloads");break;case 2:browser();break;case 3:terminal();break;case 4:camera();break;case 5:pick();break;case 6:export();break;case 7:startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));break;case 8:files("Documents");break;case 9:files("Downloads");break;case 10:files("Pictures");break;case 11:files("Projects");break;}return true;}
    }
}
