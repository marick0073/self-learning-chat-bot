import java.net.*;
import java.io.*;

class VK_RH{
    
    public static void main(String[] args) throws Exception{
            
        System.out.println(BEE.handleCMD("/load 0"));
        
        new Thread(){
            
            public void run(){
                
                try{
        
                    ServerSocket ss=new ServerSocket(80);
                    Thread trh[]=new Thread[200];
                
                    for(int i=0;true;i=(i+1)%trh.length)
                    
                        if(trh[i]==null){
                        
                            final Socket S=ss.accept();
                            final int IRH=i;
                        
                            trh[i]=new Thread(){
                        
                                public void run(){
                            
                                    try(Socket s=S){
                    
                                        String rc=null,jc[]=null;
                                        if((rc=readContent(s.getInputStream()))!=null &&
                                           (jc=VK_J.handleJSON(rc))!=null)
                                            VK_G_B.handleContent(jc,s);
                
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                            
                                    trh[IRH]=null;
                            
                                }
                        
                            };
                        
                            trh[i].start();
                        
                        }
                    
                }catch(Exception e){
                    e.printStackTrace();
                    System.exit(0);
                }
                
            }
            
        }.start();
        
        int c=-1;
        byte cmd[]=new byte[0];
        while((c=System.in.read())!=-1)
            if(c=='\n')
                try{
                    System.out.println();
                    System.out.println(BEE.handleCMD(new String(cmd,0,cmd.length)));
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    System.out.println();
                    cmd=new byte[0];
                }
            else cmd=UT.addToArrayByte(cmd,(byte)c);
        
    }
    
    private static String readContent(InputStream is) throws Exception{

        int b=-1;
        String ps="";
        int o=0;
        byte[] b_=null;

        while((b=is.read())!=-1){

            if(ps!=null){

                ps+=(char)b;

                if(ps.endsWith("\r\n\r\n")){

                    int cl=-1;

                    for(String p:ps.substring(0,ps.length()-4).split("\r\n")){

                        String pn=p,
                               pv=null;

                        if(p.contains(":")){

                             pn=p.substring(0,p.indexOf(":")).trim();
                             pv=p.substring(p.indexOf(":")+1).trim();

                        }

                        if(pn.equals("Content-Length"))cl=Integer.parseInt(pv);

                    }

                    ps=null;

                    if(cl<=0)break;
                    else b_=new byte[cl];

                }

            }
            else{

                b_[o++]=(byte)b;
                if(o==b_.length)return new String(b_);

            }

        }
        
        return null;
        
    }
    
    static void writeContent(OutputStream os, String ct) throws Exception{
        
        os.write(("HTTP/1.1 200 OK\r\n\r\n"+ct).getBytes());
        
    }
    
}

class VK_J{
    
    static String[] handleJSON(String ct){
        
        String gid=null,sk=null,tp=null,pid=null,tx=null;
        
        if((gid=getPM(ct,"group_id"))!=null && (sk=getPM(ct,"secret"))!=null && (tp=getPM(ct,"type"))!=null)
            
            if(tp.equals("confirmation"))
                return new String[]{gid,sk,tp};
            else if(tp.equals("message_new") && (pid=getPM(ct,"peer_id"))!=null && (tx=getPM(ct,"text"))!=null)
                return new String[]{gid,sk,tp,pid,tx};
    
        return null;
        
    }
    
    static String getPM(String ct, String pm){
        
        int st=0,ed=0;
        ct=encodeChars(ct);
        
        if((st=ct.indexOf("\""+pm+"\":"))>-1 && (
           ((st+=3+pm.length())+1<ct.length() && ct.charAt(st)=='\"' && (ed=ct.indexOf('\"',(st+=1)))>-1) ||
           (ed>-1 && (ed=getNumEnd(ct,st))>-1)
          ))return decodeChars(ct.substring(st,ed));
        
        return null;
        
    }
    
    private static int getNumEnd(String ct, int st){
        
        int ed=st;
        
        while(ed<ct.length() && '0'<=ct.charAt(ed) && ct.charAt(ed)<='9')ed++;
        
        return ed-st>0?ed:-1;
        
    }
    
    static String encodeChars(String scs){
        
        return scs.replace("\\\\","\u0000").replace("\\\"","\u0001").replace("\\/","\u0002").replace("\\n","\u0003");
        
    }
    
    static String decodeChars(String scs){
        
        return scs.replace("\u0000","\\").replace("\u0001","\"").replace("\u0002","/").replace("\u0003","\n");
        
    }
    
}

class VK_G_B{
    
    final static String
        GROUP_ID="?",
        GROUP_DOMAIN="?",
        SECRET_KEY="?",
        CONFIRM_CODE="?",
        ACCESS_TOKEN="?",
        OWNER_ACCESS_TOKEN="?";
    
    static void handleContent(String[] js, Socket s) throws Exception{
            
        if(js[0].equals(GROUP_ID) && js[1].equals(SECRET_KEY))
                
            if(js[2].equals("confirmation")){
                VK_RH.writeContent(s.getOutputStream(),CONFIRM_CODE);
                s.close();
            }
            else if(js[2].equals("message_new")){
                VK_RH.writeContent(s.getOutputStream(),"ok");
                s.close();
                long pid=Long.parseLong(js[3]);
                String ms=js[4];
                if(pid<2000000000)
                    sendMessage(pid,BEE.answerMessage(pid,ms,false));
                else if(ms.toLowerCase().startsWith("бии ") || ms.toLowerCase().startsWith("бии,"))
                    sendMessage(pid,BEE.answerMessage(pid,ms.substring(4).trim(),true));
                else if(ms.contains("[club"+GROUP_ID+"|@"+GROUP_DOMAIN+"]"))
                    sendMessage(pid,BEE.answerMessage(pid,ms,true));
                Thread.sleep(50);
            }
            else{
                VK_RH.writeContent(s.getOutputStream(),"UNKNOWN_CONTENT_TYPE_ERROR");
                s.close();
            }
                
        else{
            VK_RH.writeContent(s.getOutputStream(),"NO_MATCHES_FOR_THESE_GROUP_AND_SECRET");
            s.close();
        }
        
    }
    
    static void sendMessage(long pid, String ms) throws Exception{
        
        if(ms!=null)
            new URL("https://api.vk.com/method/messages.send?peer_id="+pid+"&message="+URLEncoder.encode(ms,"UTF8")+"&access_token="+ACCESS_TOKEN+"&v=5.87").openStream().close();
        
    }
    
}

class BEE{
    
    final private static int DIALOGUE_MEMORY=10;
    
    private static long users[]=new long[0];
    private static int dialogues[][][]=new int[0][0][2];
    private static String messages[]=new String[0];
    
    private static int findUser(long uid){
        
        for(int i=0;i<users.length;i++)
            if(users[i]==uid)
                return i;
        addDialogueUser();
        
        return (users=UT.addToArrayLong(users,uid)).length-1;
        
    }
    
    private static int replaceMessage(String ms, String rms){
        
        int msi=findMessage(ms);
        
        if(msi>-1 && messages[msi]!=null){
            
            int rmsi=findAddMessage(rms);
            
            for(int ui=0;ui<dialogues.length;ui++)
                for(int mi=0;mi<dialogues[ui].length;mi++)
                    if(dialogues[ui][mi][1]==msi)
                        dialogues[ui][mi][1]=rmsi;
            
            messages[msi]=null;
            
            return rmsi;
            
        }
        
        return -1;
        
    }
    
    private static int findAddMessage(String ms){
        
        int fm=findMessage(ms);
        
        if(fm>-1){
            if(messages[fm]==null)
                messages[fm]=ms;
            return fm;
        }
        
        return (messages=UT.addToArrayString(messages,ms)).length-1;
        
    }
    
    private static int findMessage(String ms){
        
        int n=-1;
        
        for(int i=0;i<messages.length;i++)
            if(messages[i]!=null){
                if(messages[i].equals(ms))
                    return i;
            }
            else if(n==-1)n=i;
        
        return n;
        
    }

    private static int addDialogueUser(){
        
        return (dialogues=UT.add2DArrayTo3DArrayInt(dialogues,new int[0][2])).length-1;
        
    }

    private static int addDialogueUserMessage(int u, int w, int mi){
        
        return (dialogues[u]=UT.addArrayTo2DArrayInt(dialogues[u],new int[]{w,mi})).length-1;
        
    }
    
    static String handleCMD(String cmd) throws Exception{
        
        if(cmd.startsWith("/"))
                
            if(cmd.equals("/stats")){
                int td=0;
                for(int ui=0;ui<dialogues.length;ui++)
                    for(int mi=0;mi<dialogues[ui].length;mi++)
                        td++;
                return "users[]: "+users.length+" элементов!\n"+
                       "dialogues[]: "+dialogues.length+" элементов!\n"+
                       " dialogues[][]: "+td+" элементов!\n"+
                       "messages[]: "+messages.length+" элементов!";
            }
            else if(cmd.startsWith("/users ") && cmd.length()>7)
                return findUser(Long.parseLong(cmd.substring(7,cmd.length())))+"";
            else if(cmd.equals("/chats")){
                cmd="";
                int cc=0;
                for(int ui=0;ui<users.length;ui++)
                    if(users[ui]>=2000000000){
                        cmd+=" "+ui+"["+users[ui]+"]";
                        cc++;
                    }
                return "chats["+cc+"]:"+cmd;
            }
                
            else if(cmd.startsWith("/dialogues ") && cmd.length()>11){
                int s1=-1,s2=-1;
                if(cmd.length()>15 && (s1=cmd.indexOf(" ",11))>-1 && (s2=cmd.indexOf(" ",s1+1))>-1)
                    return dialogues[Integer.parseInt(cmd.substring(11,s1))][Integer.parseInt(cmd.substring(s1+1,s2))][Integer.parseInt(cmd.substring(s2+1,cmd.length()))]+"";
                else if(cmd.length()>15 && (s1=cmd.indexOf(" ",11))>-1 && (s2=cmd.indexOf("-",s1+1))>-1){
                    int ui=Integer.parseInt(cmd.substring(11,s1)),
                        sm=Integer.parseInt(cmd.substring(s1+1,s2)),
                        em=Integer.parseInt(cmd.substring(s2+1,cmd.length()));
                    cmd="";
                    for(int i=sm;-1<i && i<=em && i<dialogues[ui].length;i++)
                        cmd+="\n  ["+i+"]: "+dialogues[ui][i][0]+", '"+messages[dialogues[ui][i][1]]+"';";
                    return "dialogues["+ui+"]["+sm+"-"+em+"]:\n"+cmd;
                }
                else return dialogues[Integer.parseInt(cmd.substring(11,cmd.length()))].length+"";
            }
                    
            else if(cmd.startsWith("/messages ") && cmd.length()>10){
                int q1=-1,q2=-1,q3=-1,q4=-1;
                cmd=VK_J.encodeChars(cmd);
                if(cmd.length()>12 && (q1=cmd.indexOf("\""))>-1 && (q2=cmd.indexOf("\"",q1+1))>-1)
                    if(cmd.length()>16 && (q3=cmd.indexOf("\"",q2+1))>-1 && (q4=cmd.indexOf("\"",q3+1))>-1)
                         return replaceMessage(VK_J.decodeChars(cmd.substring(q1+1,q2)),VK_J.decodeChars(cmd.substring(q3+1,q4)))+"";
                    else return findMessage(VK_J.decodeChars(cmd.substring(q1+1,q2)))+"";
                else return messages[Integer.parseInt(cmd.substring(10,cmd.length()))];
            }
                    
            else if(cmd.startsWith("/load ") && cmd.length()>6)
                return "Загружено "+loadFile(cmd.substring(cmd.indexOf(" ")+1,cmd.length()))+" элементов из файла!";
        
            else if(cmd.startsWith("/save ") && cmd.length()>6)
                return "Сохранено "+saveFile(cmd.substring(cmd.indexOf(" ")+1,cmd.length()))+" элементов в файл!";
        
            else if(cmd.startsWith("/import_file ") && cmd.length()>13)
                return "Импортировано "+importFile(cmd.substring(cmd.indexOf(" ")+1,cmd.length()))+" элементов из файла!";
        
            else if(cmd.startsWith("/import_token ") && cmd.length()>14)
                return "Импортировано "+importDialogues(cmd.substring(cmd.indexOf(" ")+1,cmd.length()))+" элементов из токена!";
        
            else return "Неизвестная команда!";
        
        else return "Команда начинается со знака \"/\"!";
        
    }
    
    static synchronized String answerMessage(long pid, String ms, boolean ic) throws Exception{
        
        for(int mesi=-1,meei=-1;((mesi=ms.indexOf("[id"))>-1 || (mesi=ms.indexOf("[club"))>-1) && (meei=ms.indexOf("]",mesi))>-1;mesi=-1,meei=-1)
            ms=ms.substring(0,mesi)+ms.substring(meei+(meei+1<ms.length() && ms.charAt(meei+1)==','?2:1)).trim();
        
        String cb=null;
        
        if(ms.length()==0)
            return "Я отвечаю только на прямые текстовые сообщения!";
        else if(ms.length()>100)
            return "Я не отвечаю на слишком длинные сообщения!";
        else if(areBadChars(ms)>-1)
            return "Я воспринимаю только стандартные символы, эмодзи и буквы русского, украинского и английского алфавитов!";
        else if((cb=areCurseWords(ms))!=null)
            if(ic){
                VK_G_B.sendMessage(pid,"Я ухожу: плохие слова всегда обхожу стороной!");
                leaveChat(pid-2000000000);
                return null;
            }
            else{
                tempBan(pid,3,"\""+cb+"\" в \""+ms+"\"!");
                return "Давай обходиться без плохих слов!?\nЗавтра спишемся!";
            }
        else if((cb=areURLS(ms))!=null)
            if(ic){
                VK_G_B.sendMessage(pid,"Я ухожу: пришёл сюда не ради \"рекламных объявлений\"!");
                leaveChat(pid-2000000000);
                return null;
            }
            else{
                tempBan(pid,1,"\""+cb+"\" в \""+ms+"\"!");
                return "\"Рекламные объявления\" тут ни к чему!\nЗавтра спишемся!";
            }
        
        int u=findUser(pid),
            sl=0,sm[]=new int[messages.length],
            sd[][]=new int[0][2];
        for(int ui=0;ui<dialogues.length;ui++)
            for(int mi=0;mi<dialogues[ui].length-1;mi++)
                if(dialogues[ui][mi+1][0]==0){
                    if(sm[dialogues[ui][mi][1]]==0)
                        sm[dialogues[ui][mi][1]]=similarity(messages[dialogues[ui][mi][1]].toLowerCase(),ms.toLowerCase());
                    if(sm[sl]<sm[dialogues[ui][mi][1]]){
                        sd=new int[][]{new int[]{ui,mi}};
                        sl=dialogues[ui][mi][1];
                    }else if(sm[sl]==sm[dialogues[ui][mi][1]])
                        sd=UT.addArrayTo2DArrayInt(sd,new int[]{ui,mi});
                }
        
        for(int dm=1;sd.length>1 && dm<DIALOGUE_MEMORY && dialogues[u].length-dm>-1;dm++){
            int slb=0;
            sl=0;
            for(int i=0;i<sd.length;i++)
                if(sd[i][1]-dm>-1){
                    if(sl<(slb=similarity(messages[dialogues[sd[i][0]][sd[i][1]-dm][1]].toLowerCase(),messages[dialogues[u][dialogues[u].length-dm][1]].toLowerCase()))){
                        sl=slb;
                        sd=UT.startArrayFrom2DArrayInt(sd,i);
                        i=0;
                    }
                    else if(sl>slb)sd=UT.delArrayFrom2DArrayInt(sd,i--);
                }
                else if(sl>0)sd=UT.delArrayFrom2DArrayInt(sd,i--);
        }
        
        addDialogueUserMessage(u,0,findAddMessage(ms));
        
        if(sd.length>0){
            int r=(int)(sd.length*Math.random());
            if(Math.random()<0.925D && sd[r][1]+1<dialogues[sd[r][0]].length){
                ms=messages[dialogues[sd[r][0]][sd[r][1]+1][1]];
                addDialogueUserMessage(u,1,dialogues[sd[r][0]][sd[r][1]+1][1]);
            }
        }
        
        return ms;
    
    }
    
    private static int areBadChars(String ms){
        
        String e1="☺☹☠⚕⛑☂✊✌☝✋✍☘⭐✨⚡☄☀⛅☁⛈☃⛄❄☔☕⚽⚾⛳⛸⛷⛹♂♀✈⛵⛴⚓⛽⛲⛱⛰⛺⛪⛩⌚⌨☎⏱⏲⏰⌛⏳⚖⚒⛏⚙⛓⚔⚰⚱⚗✉✂✒✏❤❣☮✝☪☸✡☯☦⛎♈♉♊♋♌♍♎♏♐♑♒♓⚛☢☣✴㊙㊗❌⭕⛔♨❗❕❓❔‼⁉〽⚠⚜♻✅❇✳❎Ⓜ♿ℹ0123456789#*⃣▶⏸⏯⏹⏺⏭⏮⏩⏪⏫⏬◀➡⬅⬆⬇↗↘↙↖↕↔↪↩⤴⤵➕➖➗✖™©®〰➰➿✔☑⚪⚫▪▫◾◽◼◻⬛⬜♠♣♥♦‍",
               e2="😀😃😄😁😆😅😂🤣😊😇🙂🙃😉😌😍😘😗😙😚😋😜😝😛🤑🤗🤓😎🤡🤠😏😒😞😔😟😕🙁😣😖😫😩😤😠😡😶😐😑😯😦😧😮😲😵😳😱😨😰😢😥🤤😭😓😪😴🙄🤔🤥😬🤐🤢🤧😷🤒🤕😈👿👹👺💩👻💀👽👾🤖🎃😺😸😹😻😼😽🙀😿😾🤝💄👄👅👣👀🗣👤👥🕵🤴🤵🕴🕺👯👫👭👬💑💏💋👪👚👕👖👔👗👙👘👠👡👢👞👟👒🎩🎓👑🎒👝👛👜💼👓🕶🌂🏻🏼🏽🏾👐🙌👏🙏👍👎👊🤛🤜🤞🤘👌👈👉👆👇🖐🖖👋🤙💪🖕🤳💅👂👃👶👦👧👨👩👱👴👵👲👳👮👷💂🎅👸👰👼🤰🙇💁🙅🙆🙋🤦🤷🙎🙍💇💆💃🚶🏃🏿🐶🐱🐭🐹🐰🦊🐻🐼🐨🐯🦁🐮🐷🐽🐸🙈🐵🙉🙊🐒🐔🐧🐦🐤🐣🐥🦆🦅🦉🦇🐺🐗🐴🦄🐝🐛🦋🐌🐚🐞🐜🕷🕸🐢🐍🦎🦂🦀🦑🐙🦐🐠🐟🐡🐬🦈🐳🐋🐊🐆🐅🐃🐂🐄🦌🐪🐫🐘🦏🦍🐎🐖🐐🐏🐑🐕🐩🐈🐓🦃🕊🐇🐁🐀🐿🐾🐉🐲🌵🎄🌲🌳🌴🌱🌿🍀🎍🎋🍃🍂🍁🍄🌾💐🌷🌹🥀🌻🌼🌸🌺🌎🌍🌏🌕🌖🌗🌘🌑🌒🌓🌔🌚🌝🌞🌛🌜🌙💫🌟🔥💥🌤🌥🌦🌧🌩🌨🌬💨🌪🌫🌊💧💦🍏🍎🍐🍊🍋🍌🍉🍇🍓🍈🍒🍑🍍🥝🥑🍅🍆🥒🥕🌽🌶🥔🍠🌰🥜🍯🥐🍞🥖🧀🥚🍳🥓🥞🍤🍗🍖🍕🌭🍔🍟🥙🌮🌯🥗🥘🍝🍜🍲🍥🍣🍱🍛🍚🍙🍘🍢🍡🍧🍨🍦🍰🎂🍮🍭🍬🍫🍿🍩🍪🥛🍼🍵🍶🍺🍻🥂🍷🥃🍸🍹🍾🥄🍴🍽🏀🏈🎾🏐🏉🎱🏓🏸🥅🏒🏑🏏🏹🎣🥊🥋🎿🏂🏋🤺🤼🤸🤾🏌🏄🏊🤽🚣🏇🚴🚵🎽🏅🎖🥇🥈🥉🏆🏵💍🎗🎫🎟🎪🎭🎨🎬🎤🎧🎼🎹🥁🎷🎺🎸🎻🎲🎯🎳🎮🎰🚗🚕🚙🚌🚎🏎🚓🚑🚒🚐🚚🚛🚜🛴🚲🛵🏍🚨🚔🚍🚘🚖🚡🚠🚟🚃🚋🚞🚝🚄🚅🚈🚂🚆🚇🚊🚉🚁🛩🛫🛬🚀🛰💺🛶🛥🚤🛳🚢🚧🚏🚦🚥🗺🗿🗽🗼🏰🏯🏟🎡🎢🎠🏖🏝🏔🗻🌋🏜🏕🛤🛣🏗🏭🏠🏡🏘🏚🏢🏬🏣🏤🏥🏦🏨🏪🏫🏩💒🏛🕌🕍🕋🗾🎑🏞🌅🌄🌠🎇🎆🌇🌆🏙🌃🌌🌉🌁📱📲💻🖥🖨🖱🖲🕹🗜💽💾💿📀📼📷📸📹🎥📽🎞📞📟📠📺📻🎙🎚🎛🕰📡🔋🔌💡🔦🕯🗑🛢💸💵💴💶💷💰💳💎🔧🔨🛠🔩🔫💣🔪🗡🛡🚬🏺🔮📿💈🔭🔬🕳💊💉🌡🚽🚰🚿🛁🛀🛎🔑🗝🚪🛋🛏🛌🖼🛍🛒🎁🎈🎏🎀🎊🎉🎎🏮🎐📩📨📧💌📥📤📦🏷📪📫📬📭📮📯📜📃📄📑📊📈📉🗒🗓📆📅📇🗃🗳🗄📋📁📂🗂🗞📰📓📔📒📕📗📘📙📚📖🔖🔗📎🖇📐📏📌📍🖊🖋🖌🖍📝🔍🔎🔏🔐🔒🔓💛💚💙💜🖤💔💕💞💓💗💖💘💝💟🕉🔯🕎🛐🆔🉑📴📳🈶🈚🈸🈺🈷🆚💮🉐🈴🈵🈹🈲🅰🅱🆎🆑🅾🆘📛🚫💯💢🚷🚯🚳🚱🔞📵🚭🔅🔆🚸🔱🔰🈯💹🌐💠🌀💤🏧🚾🅿🈳🈂🛂🛃🛄🛅🚹🚺🚼🚻🚮🎦📶🈁🔣🔤🔡🔠🆖🆗🆙🆒🆕🆓🔟🔢🔼🔽🔀🔁🔂🔄🔃🎵🎶💲💱🔚🔙🔛🔝🔘🔴🔵🔺🔻🔸🔹🔶🔷🔳🔲🔈🔇🔉🔊🔔🔕📣📢👁🗨💬💭🗯🃏🎴🀄🕐🕑🕒🕓🕔🕕🕖🕗🕘🕙🕚🕛🕜🕝🕞🕟🕠🕡🕢🕣🕤🕥🕦🕧🏴🏁🚩🏳🌈🇫🇩🇮🇯🎌🇾🇲🇽🇴🇱🇵🇿🇰🇨🇭🇹🇷🇦🇪🇬🇧🇺🇸🇻🇳",
               e5="🏳‍🌈",
               c1="×";
        
        for(int i=0,l=ms.codePointCount(0,ms.length());i<l;i++){
            
            int cp=ms.codePointAt(i);
            
            if(cp>=0x0000 && cp<=0x00BF)continue;
            else if(c1.indexOf(cp)>-1)continue;
            
            else if(e1.indexOf(cp)>-1)continue;
            else if(e2.indexOf(cp)>-1)continue;
            
            else if((cp>='А' && cp<='я') || cp=='Ё' || cp=='ё')continue;
            else if("ҐґЄєІіЇї".indexOf(cp)>-1)continue;
            
            else return i;
            
        }
            
        return -1;
        
    }
    
    private static String areCurseWords(String ms){
        
        ms=ms.toLowerCase();
        
        String rl="абвгдеёжзийклмнопрстуфхцчшщъыьэюяabcdefghijklmnopqrstuvwxyz";
        for(String cw:new String[]{"?"}){
            
            p:for(int i=0;i<ms.length();i++){
                int w=0;
                if(cw.length()>0 && cw.charAt(0)==' ' && i==0)w++;
                for(int g=i;w<cw.length() && g<ms.length();g++)
                    if((cw.charAt(w)==' ' && !(rl.indexOf(ms.charAt(g))>-1)) ||
                       (cw.charAt(w)!=' ' && similarChars(cw.charAt(w)).indexOf(ms.charAt(g))>-1))
                        if(++w==cw.length())break;
                        else continue;
                    else if((w>0 && similarChars(cw.charAt(w-1)).indexOf(ms.charAt(g))>-1) || 
                           !(rl.indexOf(ms.charAt(g))>-1))continue;
                    else continue p;
                if(cw.length()>0 && w==cw.length()-1 && cw.charAt(w)==' ')w++;
                if(w==cw.length())return cw;
            }
            
        }
        
        return null;
        
    }
    
    private static String similarChars(char c){
        
        for(String ca:new String[]{"аa@","бb","вbv","гg","дd",
                                   "еёe","жjg","зz","ийiu",
                                   "кk","лl","мm","нnh",
                                   "оo","пp","рpr","сcs","тt",
                                   "уyu","фf","хxh","цc","ч",
                                   "ш","щ","ъ","ыui","ь",
                                   "эa","юu","я"})
            if(ca.indexOf(c)>-1)return ca;
        
        return String.valueOf(c);
        
    }
    
    private static String areURLS(String ms){
        
        ms=ms.toLowerCase();
        
        String rl="абвгдеёжзийклмнопрстуфхцчшщъыьэюяabcdefghijklmnopqrstuvwxyz";
        for(String cw:new String[]{"https://","http://",".ac ",".ad ",".af ",".ag ",".ai ",".al ",".an ",".ao ",".aq ",".ar ",".as ",".at ",".au ",".aw ",".ax ",".az ",".ba ",".bb ",".be ",".bf ",".bh ",".bi ",".bj ",".bl ",".bm ",".bn ",".bo ",".bq ",".br ",".bs ",".bt ",".bv ",".bw ",".cc ",".cd ",".cf ",".cg ",".ch ",".ci ",".ck ",".cl ",".cm ",".co ",".cr ",".cu ",".cv ",".cw ",".cx ",".cy ",".cz ",".de ",".dj ",".dk ",".dm ",".do ",".ec ",".ee ",".eh ",".er ",".es ",".et ",".fi ",".fj ",".fk ",".fm ",".fo ",".fr ",".ga ",".gb ",".gd ",".gf ",".gg ",".gh ",".gi ",".gl ",".gm ",".gn ",".gp ",".gq ",".gs ",".gt ",".gu ",".gw ",".gy ",".hm ",".hn ",".hr ",".ht ",".hu ",".id ",".ie ",".il ",".im ",".io ",".is ",".it ",".je ",".jm ",".jp ",".ke ",".kg ",".kh ",".ki ",".km ",".kn ",".kp ",".kw ",".ky ",".la ",".lb ",".lc ",".li ",".lr ",".ls ",".lt ",".lu ",".lv ",".ly ",".mc ",".md ",".me ",".mf ",".mg ",".mh ",".ml ",".mm ",".mp ",".mq ",".ms ",".mt ",".mu ",".mv ",".mw ",".mx ",".mz ",".na ",".nc ",".ne ",".nf ",".ng ",".ni ",".nl ",".np ",".nr ",".nu ",".nz ",".pa ",".pe ",".pf ",".pg ",".ph ",".pl ",".pm ",".pn ",".pr ",".pt ",".pw ",".py ",".re ",".ro ",".rw ",".sb ",".sc ",".se ",".sh ",".si ",".sj ",".no ",".sk ",".sl ",".sm ",".sn ",".so ",".sr ",".ss ",".st ",".bz ",".su ",".sv ",".sx ",".sz ",".tc ",".td ",".tf ",".tg ",".tj ",".tk ",".tm ",".to ",".tp ",".tl ",".tr ",".tt ",".tv ",".tz ",".ug ",".uk ",".um ",".us ",".uy ",".uz ",".va ",".vc ",".ve ",".vg ",".vi ",".vn ",".vu ",".wf ",".ws ",".yt ",".za ",".zw ",".zm ",".dz ",".am ",".bd ",".бел ",".by ",".bg ",".cn ",".eg ",".ею ",".eu ",".ge ",".gr ",".hk ",".in ",".ir ",".iq ",".jo ",".kz ",".mo ",".мкд ",".mk ",".my ",".mr ",".мон ",".mn ",".ma ",".om ",".pk ",".ps ",".qa ",".sa ",".срб ",".rs ",".sg ",".kr ",".lk ",".sd ",".sy ",".tw ",".th ",".tn ",".укр ",".ua ",".ae ",".ye ",".academy ",".accountant ",".accountants ",".active ",".actor ",".ads ",".adult ",".agency ",".airforce ",".analytics ",".apartments ",".archi ",".army ",".associates ",".attorney ",".auction ",".audible ",".audio ",".author ",".auto ",".autos ",".aws ",".baby ",".band ",".bank ",".barefoot ",".bargains ",".baseball ",".basketball ",".beauty ",".beer ",".best ",".bestbuy ",".bet ",".bid ",".bike ",".bingo ",".bio ",".black ",".blackfriday ",".blockbuster ",".blog ",".blue ",".boo ",".book ",".boots ",".boutique ",".box ",".broadway ",".broker ",".build ",".builders ",".business ",".buy ",".buzz ",".cafe ",".call ",".cam ",".camera ",".camp ",".cancerresearch ",".capital ",".car ",".cards ",".care ",".career ",".careers ",".cars ",".case ",".cash ",".casino ",".catering ",".catholic ",".center ",".cern ",".ceo ",".cfd ",".channel ",".chat ",".cheap ",".christmas ",".cipriani ",".circle ",".city ",".claims ",".cleaning ",".click ",".clinic ",".clothing ",".cloud ",".coach ",".codes ",".coffee ",".community ",".company ",".compare ",".computer ",".condos ",".construction ",".consulting ",".contact ",".contractors ",".cooking ",".cool ",".country ",".coupon ",".coupons ",".courses ",".credit ",".creditcard ",".cruise ",".cricket ",".cruises ",".dad ",".dance ",".date ",".dating ",".day ",".deal ",".deals ",".degree ",".delivery ",".democrat ",".dental ",".dentist ",".diamonds ",".diet ",".digital ",".direct ",".directory ",".discount ",".diy ",".docs ",".doctor ",".dog ",".domains ",".dot ",".drive ",".duck ",".earth ",".eat ",".education ",".email ",".energy ",".engineer ",".engineering ",".enterprises ",".equipment ",".esq ",".estate ",".events ",".exchange ",".expert ",".exposed ",".express ",".fail ",".faith ",".family ",".fan ",".fans ",".farm ",".fashion ",".fast ",".feedback ",".film ",".final ",".finance ",".financial ",".fire ",".fish ",".fishing ",".fit ",".fitness ",".flights ",".florist ",".flowers ",".fly ",".foo ",".food ",".foodnetwork ",".football ",".forsale ",".forum ",".foundation ",".free ",".frontdoor ",".fun ",".fund ",".furniture ",".fyi ",".gallery ",".game ",".games ",".garden ",".gdn ",".gift ",".gifts ",".gives ",".glass ",".global ",".gold ",".golf ",".gop ",".graphics ",".gripe ",".grocery ",".group ",".guide ",".guitars ",".guru ",".hair ",".hangout ",".health ",".healthcare ",".help ",".here ",".hiphop ",".hockey ",".holdings ",".holiday ",".homegoods ",".homes ",".homesense ",".horse ",".hospital ",".host ",".hosting ",".hot ",".hotels ",".house ",".how ",".ice ",".industries ",".ing ",".ink ",".institute ",".insurance ",".insure ",".international ",".investments ",".jewelry ",".joy ",".kim ",".kitchen ",".land ",".latino ",".law ",".lawyer ",".lease ",".legal ",".life ",".lifeinsurance ",".lighting ",".like ",".limited ",".limo ",".link ",".live ",".living ",".loan ",".loans ",".locker ",".lol ",".lotto ",".love ",".luxury ",".makeup ",".management ",".map ",".market ",".marketing ",".markets ",".mba ",".med ",".media ",".meet ",".meme ",".memorial ",".menu ",".mint ",".mobile ",".mobily ",".mom ",".money ",".mortgage ",".motorcycles ",".mov ",".movie ",".navy ",".network ",".new ",".news ",".now ",".observer ",".off ",".ngo ",".ong ",".onl ",".online ",".open ",".organic ",".origins ",".page ",".partners ",".parts ",".party ",".pay ",".pet ",".pharmacy ",".phone ",".photo ",".photography ",".photos ",".physio ",".pics ",".pictures ",".pid ",".pin ",".pink ",".pizza ",".place ",".plumbing ",".plus ",".poker ",".porn ",".press ",".prime ",".productions ",".prof ",".promo ",".properties ",".property ",".protection ",".qpon ",".racing ",".radio ",".read ",".realestate ",".realtor ",".realty ",".recipes ",".rehab ",".reit ",".ren ",".rent ",".rentals ",".repair ",".report ",".republican ",".rest ",".restaurant ",".review ",".reviews ",".rich ",".rip ",".rocks ",".rodeo ",".room ",".rugby ",".run ",".safe ",".sale ",".save ",".scholarships ",".school ",".science ",".search ",".secure ",".security ",".select ",".services ",".sex ",".sexy ",".shoes ",".shopping ",".show ",".showtime ",".silk ",".singles ",".site ",".ski ",".skin ",".sky ",".sling ",".smile ",".soccer ",".social ",".software ",".solar ",".solutions ",".song ",".space ",".spot ",".spreadbetting ",".storage ",".store ",".stream ",".studio ",".study ",".style ",".sucks ",".supplies ",".supply ",".support ",".surf ",".surgery ",".systems ",".talk ",".tattoo ",".tax ",".taxi ",".team ",".tech ",".technology ",".tennis ",".theater ",".theatre ",".tickets ",".tips ",".tires ",".today ",".tools ",".top ",".tours ",".town ",".toys ",".trade ",".trading ",".training ",".travelersinsurance ",".trust ",".tube ",".tunes ",".uconnect ",".university ",".vacations ",".ventures ",".vet ",".video ",".villas ",".vip ",".vision ",".vodka ",".vote ",".voting ",".voyage ",".wang ",".watch ",".watches ",".weather ",".webcam ",".website ",".wed ",".wedding ",".whoswho ",".win ",".wine ",".winners ",".work ",".works ",".world ",".wow ",".yachts ",".yoga ",".you ",".zero ",".zone ",".shouji ",".tushu ",".wanggou ",".weibo ",".xihuan ",".arte ",".clinique ",".luxe ",".maison ",".moi ",".rsvp ",".sarl ",".epost ",".gmbh ",".haus ",".immobilien ",".jetzt ",".kinder ",".reise ",".reisen ",".schule ",".versicherung ",".desi ",".shiksha ",".casa ",".immo ",".moda ",".voto ",".bom ",".passagens ",".abogado ",".gratis ",".futbol ",".hoteles ",".juegos ",".ltda ",".soy ",".tienda ",".uno ",".viajes ",".vuelos ",".дети ",".ком ",".онлайн ",".орг ",".сайт ",".capetown ",".durban ",".joburg ",".abudhabi ",".arab ",".doha ",".dubai ",".kyoto ",".nagoya ",".okinawa ",".osaka ",".ryukyu ",".taipei ",".tatar ",".yokohama ",".alsace ",".barcelona ",".bayern ",".budapest ",".cologne ",".corsica ",".hamburg ",".helsinki ",".koeln ",".madrid ",".nrw ",".ruhr ",".stockholm ",".swiss ",".tirol ",".zuerich ",".miami ",".vegas ",".melbourne ",".sydney ",".москва ",".moscow ",".рус ",".ru ",".рф ",".aaa ",".abarth ",".abb ",".abc ",".aeg ",".afl ",".aig ",".airtel ",".americanexpress ",".bananarepublic ",".bbc ",".bentley ",".ca ",".app ",".bar ",".bible ",".biz ",".church ",".club ",".college ",".com ",".design ",".dev ",".download ",".eco ",".eus ",".google ",".green ",".hiv ",".info ",".kaufen ",".lgbt ",".moe ",".name ",".net ",".ninja ",".one ",".ooo ",".org ",".pro ",".wiki ",".wtf ",".xyz ",".aero ",".asia ",".cat ",".coop ",".edu ",".gov ",".int ",".jobs ",".mil ",".mobi ",".museum ",".post ",".tel ",".travel ",".xxx ",".africa ",".amsterdam ",".berlin ",".brussels ",".bzh ",".cymru ",".frl ",".gal ",".gent ",".irish ",".istanbul ",".kiwi ",".krd ",".london ",".nyc ",".paris ",".quebec ",".rio ",".saarland ",".scot ",".tokyo ",".vlaanderen ",".wales ",".wien ",".arpa ",".nato ",".example ",".invalid ",".local ",".localhost ",".onion ",".test ",".bcn ",".lat ",".eng ",".sic ",".бг ",".geo ",".mail ",".web ",".shop ",".art ",".kid ",".kids ",".music "}){
            
            p:for(int i=0;i<ms.length();i++){
                int w=0;
                if(cw.length()>0 && cw.charAt(0)==' ' && i==0)w++;
                for(int g=i;w<cw.length() && g<ms.length();g++)
                    if((cw.charAt(w)==' ' && !(rl.indexOf(ms.charAt(g))>-1)) ||
                       (cw.charAt(w)!=' ' && ms.charAt(g)==cw.charAt(w)))
                        if(++w==cw.length())break;
                        else continue;
                    else if((w>0 && ms.charAt(g)==cw.charAt(w-1)) || 
                           !(rl.indexOf(ms.charAt(g))>-1))continue;
                    else continue p;
                if(cw.length()>0 && w==cw.length()-1 && cw.charAt(w)==' ')w++;
                if(w==cw.length())return cw;
            }
            
        }
        
        return null;
        
    }
    
    private static int leaveChat(long cid) throws Exception{
        
        try(InputStream is=new URL("https://api.vk.com/method/messages.removeChatUser?chat_id="+cid+"&member_id=-"+VK_G_B.GROUP_ID+"&access_token="+VK_G_B.ACCESS_TOKEN+"&v=5.87").openStream()){
            
            return 1;
            
        }finally{
            
            Thread.sleep(50);
            
        }
        
    }
    
    private static int tempBan(long uid, int r, String c) throws Exception{
        
        try(InputStream is=new URL("https://api.vk.com/method/groups.ban?group_id="+VK_G_B.GROUP_ID+"&owner_id="+uid+"&reason="+r+"&comment="+URLEncoder.encode(c,"UTF8")+"&end_date="+(System.currentTimeMillis()/1000+24*60*60)+"&access_token="+VK_G_B.OWNER_ACCESS_TOKEN+"&v=5.87").openStream()){
            
            return 1;
            
        }finally{
            
            Thread.sleep(350);
            
        }
        
    }
    
    private static int similarity(String w, String ws){
        
        if(w!=null && w.length()>0 && ws!=null && ws.length()>0){
        
            int[][] wp = new int[w.length()][2];
        
            for(int c=0;c<wp.length;c++){
                int np=-1;
                for(int d=c-1;d>-1;d--)
                    if(wp[d][1]==1){
                        np=wp[d][0];
                        break;
                    }
                wp[c][0]=ws.indexOf(w.charAt(c),np+1);
                if(wp[c][0]>np){
                    wp[c][1]=1;
                    np=c;
                    for(int d=c-1;d>-1;d--)
                        if(wp[d][1]==1){
                            wp[d][0]=wp[d][0]+ws.substring(wp[d][0],wp[np][0]).lastIndexOf(w.charAt(d));
                            np=d;
                        }
                }else{
                    wp[c][0]=0;
                lnc:while((np=(wp[c][0]=ws.lastIndexOf(w.charAt(c),np-1)))>-1){
                        for(int d=c-1;d>-1;d--)
                            if(wp[d][0]==wp[c][0])
                                continue lnc;
                        break;
                    }
                }
            }
        
            for(int c=1;c<wp.length;c++)
                if(wp[c][1]==1){
                    int np=-1;
                    for(int d=c-1;d>-1;d--)
                        if(wp[d][1]==1){
                            np=wp[d][0];
                            break;
                        }
                    wp[c][0]=ws.indexOf(w.charAt(c),np+1);
                }
            
            int s=1, sc=(Integer.MAX_VALUE-1)/w.length(), scd=sc/(w.length()+ws.length()-1);
        
            for(int c=0;c<wp.length;c++)
                if(wp[c][0]>-1)
                    s+=sc-scd*(Math.abs(c-wp[c][0])+Math.abs((w.length()-c)-(ws.length()-wp[c][0])));
            
            return s;
            
        }
        
        return 1;
        
    }
    
    static long loadFile(String fn) throws Exception{
        
        try(InputStream is=new FileInputStream("BEE_"+fn+".dat")){
            
            byte[] bi=new byte[4],bl=new byte[8];
           
            is.read(bi);
            users=new long[UT.toIntByteArray(bi)];
            for(int i=0;i<users.length;i++){
                is.read(bl);
                users[i]=UT.toLongByteArray(bl);
            }
            
            is.read(bi);
            dialogues=new int[UT.toIntByteArray(bi)][][];
            for(int u=0;u<dialogues.length;u++){
                is.read(bi);
                dialogues[u]=new int[UT.toIntByteArray(bi)][2];
                for(int m=0;m<dialogues[u].length;m++){
                    is.read(bi);
                    dialogues[u][m][0]=UT.toIntByteArray(bi);
                    is.read(bi);
                    dialogues[u][m][1]=UT.toIntByteArray(bi);
                }
            }
            
            is.read(bi);
            messages=new String[UT.toIntByteArray(bi)];
            for(int i=0;i<messages.length;i++){
                is.read(bi);
                if(UT.toIntByteArray(bi)>-1){
                    byte[] bm=new byte[UT.toIntByteArray(bi)];
                    is.read(bm);
                    messages[i]=new String(bm);
                }
                else messages[i]=null;
            }
            
            return 1;
            
        }
        
    }
    
    static long saveFile(String fn) throws Exception{
        
        try(OutputStream os=new FileOutputStream("BEE_"+fn+".dat")){
            
            os.write(UT.toByteArrayInt(users.length));
            for(int i=0;i<users.length;i++)
                os.write(UT.toByteArrayLong(users[i]));
            
            os.write(UT.toByteArrayInt(dialogues.length));
            for(int u=0;u<dialogues.length;u++){
                os.write(UT.toByteArrayInt(dialogues[u].length));
                for(int m=0;m<dialogues[u].length;m++){
                    os.write(UT.toByteArrayInt(dialogues[u][m][0]));
                    os.write(UT.toByteArrayInt(dialogues[u][m][1]));
                }
            }
            
            os.write(UT.toByteArrayInt(messages.length));
            for(int i=0;i<messages.length;i++)
                if(messages[i]!=null){
                    byte[] b=messages[i].getBytes();
                    os.write(UT.toByteArrayInt(b.length));
                    os.write(b);
                }
                else os.write(UT.toByteArrayInt(-1));
            
            return 1;
            
        }
        
    }
    
    private static long importDialogues(String at) throws Exception{
        
        String[] u=new String[0],m=new String[0];
        for(int od=0;(u=getDialogs(od,200,at)).length>0;od+=200)
            for(int ui=0;ui<u.length;ui++){
                
                System.out.println("["+(od+ui)+"] vk.com/id"+u[ui]);
                
                int uin=findUser(Long.parseLong(u[ui]));
                StringBuilder lo=new StringBuilder(""),ms=new StringBuilder("");
                for(int om=0;(m=getHistory(u[ui],om,200,at,lo,ms)).length>0;om+=200)
                    for(int mi=0;mi<m.length;mi++)
                        if(m[mi].length()>0)
                            addDialogueUserMessage(uin,0,findAddMessage(m[mi]));
                
                if(ms.length()>0)
                    addDialogueUserMessage(uin,0,findAddMessage(ms.toString()));
                
            }
        
        return 1;
        
    }
    
    private static String[] getDialogs(int o,int c, String at) throws Exception{
        
        try(InputStream is=new URL("https://api.vk.com/method/messages.getDialogs?offset="+o+"&count="+c+"&access_token="+at+"&v=5.78").openStream()){
            
            int bl=0,bp=0;
            byte[] bi=new byte[2048];
            while((bl=is.read(bi,bp,bi.length-bp))!=-1)
                if((bp+=bl)==bi.length)
                    bi=UT.createArrayBytes(bi,2048);
            
            int p=0;
            String si=VK_J.encodeChars(new String(bi,0,bp)),ua[]=new String[0];
            while((p=si.indexOf("\"id\":",p))>-1)
                if(si.substring((p=si.indexOf(",",p+5))+1,p+8).equals("\"date\":"))
                    ua=UT.addToArrayString(ua,si.substring(p=si.indexOf("\"user_id\":",p)+10,si.indexOf(",",p)));
            
            return ua;
            
        }finally{
            
            Thread.sleep(50);
            
        }
        
    }
    
    private static String[] getHistory(String uid, int o, int c, String at, StringBuilder lo, StringBuilder m) throws Exception{
        
        try(InputStream is=new URL("https://api.vk.com/method/messages.getHistory?offset="+o+"&count="+c+"&user_id="+uid+"&rev=1&access_token="+at+"&v=5.78").openStream()){
            
            int bl=0,bp=0;
            byte[] bi=new byte[2048];
            while((bl=is.read(bi,bp,bi.length-bp))!=-1)
                if((bp+=bl)==bi.length)
                    bi=UT.createArrayBytes(bi,2048);
            
            int p=0;
            String si=VK_J.encodeChars(new String(bi,0,bp)),ma[]=new String[0];
            while((p=si.indexOf("\"id\":",p))>-1)
                if(si.substring(p=si.indexOf(",",p+5)+1,p+7).equals("\"body\":")){
                    String bo=VK_J.decodeChars(si.substring(p=si.indexOf("\"body\":",p)+8,si.indexOf("\"",p))),
                           ou=si.substring(p=si.indexOf("\"out\":",p)+6,p+1);
                    if(ou.equals(lo.toString()))
                        m.append(" "+bo);
                    else{
                        if(lo.length()>0)ma=UT.addToArrayString(ma,m.toString());
                        m.delete(0,m.length()).append(bo);
                        lo.delete(0,lo.length()).append(ou);
                    }
                }
            
            return ma;
            
        }finally{
            
            Thread.sleep(50);
            
        }
        
    }
    
    private static long importFile(String fn) throws Exception{
        
        try(InputStream is=new FileInputStream(fn)){
            
            int bl=0,bp=0;
            byte[] bi=new byte[2048];
            while((bl=is.read(bi,bp,bi.length-bp))!=-1)
                if((bp+=bl)==bi.length)
                    bi=UT.createArrayBytes(bi,2048);
            
            int uin=findUser(0);
            int s=0;
            for(int i=0;i<bp;i++)
                if(i-s>2 && bi[i-2]==92 && bi[i-1]==48 && bi[i]==10){
                    String qa=new String(bi,s,i-s+1),
                           q=qa.substring(0,qa.indexOf("\\")),
                           a=qa.substring(qa.indexOf("\\")+1,qa.indexOf("\\0\n"));
                    s=i+1;
                    if(q.length()>0 && q.length()<100 &&
                       a.length()>0 && a.length()<100){
                        addDialogueUserMessage(uin,0,findAddMessage(q));
                        addDialogueUserMessage(uin,0,findAddMessage(a));
                    }
                }
         
            return 1;
            
        }
        
    }
    
}

class UT{
    
    static byte[] createArrayBytes(byte[] a, int e){
        
        byte[] b=new byte[a.length+e];
        for(int i=0;i<a.length;i++)
            b[i]=a[i];
        
        return b;
        
    }
    
    static byte[] addToArrayByte(byte[] a, byte e){
        
        byte[] b=new byte[a.length+1];
        for(int i=0;i<a.length;i++)
            b[i]=a[i];
        b[a.length]=e;
        
        return b;
        
    }
    
    static int[][] addArrayTo2DArrayInt(int[][] a, int[] e){
        
        int[][] b=new int[a.length+1][];
        for(int i=0;i<a.length;i++)
            b[i]=a[i];
        b[a.length]=e;
        
        return b;
        
    }
    
    static int[][] delArrayFrom2DArrayInt(int[][] a, int e){
        
        int[][] b=new int[a.length-1][];
        for(int i=0;i<e;i++)
            b[i]=a[i];
        for(int i=e+1;i<a.length;i++)
            b[i-1]=a[i];
        
        return b;
        
    }
    
    static int[][] startArrayFrom2DArrayInt(int[][] a, int e){
        
        int[][] b=new int[a.length-e][];
        for(int i=e;i<a.length;i++)
            b[i-e]=a[i];
        
        return b;
        
    }
    
    static int[][][] add2DArrayTo3DArrayInt(int[][][] a, int[][] e){
        
        int[][][] b=new int[a.length+1][][];
        for(int i=0;i<a.length;i++)
            b[i]=a[i];
        b[a.length]=e;
        
        return b;
        
    }
    
    static long[] addToArrayLong(long[] a, long e){
        
        long[] b=new long[a.length+1];
        for(int i=0;i<a.length;i++)
            b[i]=a[i];
        b[a.length]=e;
        
        return b;
        
    }
    
    static String[] addToArrayString(String[] a, String e){
        
        String[] b=new String[a.length+1];
        for(int i=0;i<a.length;i++)
            b[i]=a[i];
        b[a.length]=e;
        
        return b;
        
    }
        
    static byte[] toByteArrayInt(int i){
        
        return new byte[]{(byte)(i >> 8*3 & 255),(byte)(i >> 8*2 & 255),(byte)(i >> 8*1 & 255),(byte)(i & 255)};
        
    }
    
    static byte[] toByteArrayLong(long l){
        
        return new byte[]{(byte)(l >> 8*7 & 255),(byte)(l >> 8*6 & 255),(byte)(l >> 8*5 & 255),(byte)(l >> 8*4 & 255),
                          (byte)(l >> 8*3 & 255),(byte)(l >> 8*2 & 255),(byte)(l >> 8*1 & 255),(byte)(l & 255)};
        
    }
    
    static int toIntByteArray(byte[] a){
        
        return ((((((a[0] & 255)<<8)+(a[1] & 255))<<8)+(a[2] & 255))<<8)+(a[3] & 255);
        
    }
    
    static long toLongByteArray(byte[] a){
        
        return ((((((((((((((a[0] & 255L)<<8)+(a[1] & 255))<<8)+(a[2] & 255))<<8)+(a[3] & 255))<<8)+(a[4] & 255))<<8)+(a[5] & 255))<<8)+(a[6] & 255))<<8)+(a[7] & 255);
        
    }
    
}
