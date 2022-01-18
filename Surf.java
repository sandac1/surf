package com.example.mygw;

import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Surf {
    SurfFrame surfFrame;
    Bkg bkg;
    Player player;
    volatile SurfState surfState;

    int width;
    int height;
    BufferedImage canvasImage;
    Graphics canvasGraphics;

    java.util.List<Sprite> spriteList;

    ImgFactory imgFactory;
    int gFps;

    int gridSize;

    int gridWidth;

    int gridHeight;

    int curGridX;

    int curGridY;

    List<Sprite> npcs;

    List<Sprite> foes;

    List<Sprite> tops;

    List<Sprite> bottoms;

    List<Sprite> all;

    int lives ;

    int boosts ;

    Random rand;

    public Surf() {
        try {
            init();
        } catch (Exception e) {
        }
    }

    public void init() throws IOException {
        width = 1600;
        height = 1000;
        gFps = 25;
        gridSize = 16;

        gridWidth = 3 * width;
        gridHeight = 2 * height;
        spriteList = new ArrayList<>();
        npcs = new ArrayList<>();
        foes = new ArrayList<>();
        tops = new ArrayList<>();
        bottoms = new ArrayList<>();

        all = new ArrayList<>();

        rand = new Random(System.currentTimeMillis());

        imgFactory = new ImgFactory();
        bkg = new Bkg(width, height, this);
        spriteList.add(bkg);
        player = new Player(width, height, this);
        spriteList.add(player);
        canvasImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        canvasGraphics = canvasImage.getGraphics();
        surfFrame = new SurfFrame(this);

        changeSurfState(SurfState.MENU);
    }

    static class SurfFrame extends JFrame {
        Surf surf;
        private final SurfPanel surfPanel;
        public SurfFrame(Surf s) {
            surf = s;
            this.setTitle("surf");
            this.setVisible(true);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            surfPanel = new SurfPanel(surf);
            this.add(surfPanel);
            surfPanel.setFocusable(true);
            this.surfPanel.setSize(surf.width, surf.height);
            this.surfPanel.setLayout(null);
            this.setSize(new Dimension(surf.width + 20, surf.height + 30));
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    surf.processMouse(e);
                    System.out.println(Thread.currentThread().getName());
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    super.mouseDragged(e);
                    System.out.println("mouseDragged");
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    super.mouseMoved(e);
                    System.out.println("mouseMoved");
                }
            });
            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    super.keyReleased(e);
                    System.out.println("pressed" + e.getKeyChar());
                    surf.processKeyEvent(e);
                }
            });
        }
    }

    static class SurfPanel extends JPanel {
        Surf surf;

        public SurfPanel(Surf s) {
            surf = s;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.drawImage(surf.canvasImage, 0, 0, surf.width, surf.height, null);
        }
    }

    static class Player extends Sprite {
        int speedX;
        int speedY;
        volatile State state;
        volatile RunState runState;
        BufferedImage[][] skateBoardImageArr;
        BufferedImage[][] playerImgArr;

        int roleId;
        int skId;
        int skFps;
        int skFrameCnt;
        Surf surf;

        static enum State {READY, RUNNING, DIE;}

        static enum RunState {STOP, LEFT_45, LEFT_30, FRONT, RIGHT_30, RIGHT_45, SLIPPED, SIT, JUMP_DOWN, JUMP_UP;}

        public Player() {
        }

        public Player(int w, int h, Surf s) {
            x = w / 2;
            y = (int)(h*0.72)/ 2;
            this.width = 64;
            this.height = 64;
            state = State.READY;
            runState = RunState.FRONT;
            speedX = 0;
            speedY = 10;
            surf = s;
//            box = new Rectangle(x + 32 - 10, y + 32, 20, 20);
            hitbox = new Rectangle(x-10,y,20,20);
            skateBoardImageArr = surf.imgFactory.skateBoardImageArr;
            playerImgArr = surf.imgFactory.playerImageArr;
            skFps = 6;
            skId = 0;
            roleId = 0;
        }

        public void setRunState(RunState runState) {
            if (this.runState != runState) {
                skId = 0;
                skFrameCnt = 0;
            }
            this.runState = runState;
            switch (runState) {
                case STOP: {
                    speedX = 0;
                    speedY = 0;
                    break;
                }
                case FRONT: {
                    speedX = 0;
                    speedY = 10;
                    break;
                }
                case LEFT_30: {
                    speedX = -5;
                    speedY = 9;
                    break;
                }
                case LEFT_45: {
                    speedX = -7;
                    speedY = 7;
                    break;
                }
                case RIGHT_30: {
                    speedX = 5;
                    speedY = 9;
                    break;
                }
                case RIGHT_45: {
                    speedX = 7;
                    speedY = 7;
                    break;
                }
                default:
                    break;
            }
        }

        @Override
        public void draw(Graphics g,Surf surf) {
            g.drawImage(skateBoardImageArr[runState.ordinal()][skId], this.x - this.width/2, this.y - this.height/2, 64, 64, null);
            g.drawImage(playerImgArr[roleId][runState.ordinal()], this.x - this.width/2, this.y - this.height/2,  64, 64, null);
            g.setColor(Color.RED);
            g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
        }

        @Override
        public int getOrder() {
            return y + 64;
        }

        @Override
        public void update(Surf surf) {
            skFrameCnt++;
            if (skFrameCnt == surf.gFps / skFps) {
                skId = (skId + 1) % 3;
                skFrameCnt = 0;
            }
        }

        public Player reset(){
            state = State.READY;
            runState = RunState.FRONT;
            speedX = 0;
            speedY = 10;
            skFps = 6;
            skId = 0;
            roleId = 0;
            return this;
        }
    }

    enum SurfState {MENU, PLAY, PAUSE, OVER;}

    static class Bkg extends Sprite {
        BufferedImage blockImg;
        int imgWidth, imgHeight;
        Surf surf;

        public Bkg(int w, int h, Surf s) {
            try {
//                blockImg = ImageIO.read(new FileInputStream("D:\\GitRepo\\MP\\LW61_wechatmp-gateway\\src\\test\\java\\com\\mp\\gateway\\bg.png"));
                blockImg = ImgFactory.bkgImg;
                width = w;
                height = h;
                imgWidth = blockImg.getWidth();
                imgHeight = blockImg.getHeight();
                surf = s;
                x = 0;
                y = 0;
            } catch (Exception e) {
                System.out.println("init Bkg error");
            }
        }

        @Override
        public void draw(Graphics g,Surf surf) {
            int xc = (surf.canvasImage.getWidth() - x) / imgWidth + 1;
            int yc = (surf.canvasImage.getHeight() - y) / imgWidth + 1;
            for (int i = 0; i < xc; i++) {
                for (int j = 0; j < yc; j++) {
                    g.drawImage(blockImg, x + i * imgWidth, y + j * imgHeight, imgWidth, imgHeight, null);
//                    g.drawRect(x + i * imgWidth, y + j * imgHeight, imgWidth, imgHeight);
                }
            }
        }

        @Override
        public int getOrder() {
            return Integer.MIN_VALUE;
        }

        @Override
        public void update(Surf s) {
            x = x - surf.player.speedX;
            y = y - surf.player.speedY;
            if (x > 0) {
                x = x - imgWidth;
            }
            if (x < -imgWidth) {
                x = x + imgWidth;
            }
            if (y < -imgHeight) {
                y = y + imgHeight;
                System.out.println("y:" + y);
            }
        }
    }

    static abstract class Sprite {
        int x, y;
        int width, height;
        Rectangle hitbox;

        Sprite effect;

        Sprite() {
        }

        abstract void draw(Graphics g,Surf surf);

        int getOrder() {
            return y;
        };

        void update(Surf surf) {
        };

        void move(Surf surf){ }
    }

    public void changeSurfState(SurfState surfState){
        switch (surfState){
            case MENU:
                triggerMenu();
                break;
            case PLAY:
                triggerPlay();
                break;
            case PAUSE:
                triggerPause();
                break;
            case OVER:
                triggerOver();
                break;
            default:
                break;
        }
    }

    public void render(){
        surfFrame.surfPanel.repaint();
    }

    public void triggerMenu(){
        resetSurf();
        surfState = SurfState.MENU;
    }

    public void triggerPlay(){
        if(SurfState.PLAY==surfState) return;
        if(SurfState.MENU == surfState){
            triggerStart();
        }
        surfState = SurfState.PLAY;
    }

    public void triggerPause(){
        if(null == surfState || (SurfState.PLAY!=surfState && SurfState.PAUSE!=surfState)) return;
        if(SurfState.PAUSE==surfState){
            surfState = SurfState.PLAY;
        }else if(SurfState.PLAY == surfState){
            surfState = SurfState.PAUSE;
        }
    }

    public void triggerOver(){
        surfState = SurfState.OVER;
    }

    public void triggerStart(){
        resetSurf();
        buildStart();
    }

    public void updateMenu(){
//        System.out.println("update Menu");
//        Graphics2D g = (Graphics2D) surfFrame.surfPanel.getGraphics();
        Graphics2D g = (Graphics2D) canvasImage.getGraphics();
        g.setColor(Color.BLACK);
        g.drawString("press enter to play",width/2-60,height/2);
        g.dispose();
        render();
    }

    public void updatePlay(){
        checkCollisions();
        buildNextScene();
        updateSprites();
        drawSprites();
        render();
    }

    public void updatePause(){
        //drawSprites();
        Graphics2D g = (Graphics2D) canvasImage.getGraphics();
        g.setColor(Color.BLACK);
        g.drawString("PAUSE!",width/2-30,height/2);
        g.dispose();
        render();
    }

    public void updateOver(){
        Graphics2D g = (Graphics2D) canvasImage.getGraphics();
        g.setColor(Color.BLACK);
        g.drawString("OVER!",width/2-30,height/2);
        g.dispose();
        render();
    }

    public void loop() {
        try {
            while (true){
                switch (surfState){
                    case MENU:
                        updateMenu();
                        break;
                    case PLAY:
                        updatePlay();
                        break;
                    case PAUSE:
                        updatePause();
                        break;
                    case OVER:
                        updateOver();
                        break;
                    default:
                        break;
                }
                Thread.sleep(1000 / gFps);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void drawSprites() {
        Graphics2D g = (Graphics2D) canvasImage.getGraphics();
        g.setBackground(Color.LIGHT_GRAY);
        g.clearRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
        spriteList.sort(Comparator.comparing(Sprite::getOrder));
        spriteList.forEach(a -> a.draw(g,this));
        g.dispose();
    }

    public void updateSprites() {
        spriteList.forEach(s->s.update(this));
    }

    public void processMouse(MouseEvent e) {
        int xx = e.getX();
        int yy = e.getY();
        if (surfState == SurfState.PLAY) {
            if (yy < height / 2) {
                player.setRunState(Player.RunState.STOP);
            } else {
                int area = (xx / (width / 5)) + 1;
                player.setRunState(Player.RunState.values()[area]);
            }
        }
    }

    public void processKeyEvent(KeyEvent e){
        switch (e.getKeyCode()){
            case KeyEvent.VK_SPACE:
                changeSurfState(SurfState.PLAY);
                break;
            case KeyEvent.VK_P:
                changeSurfState(SurfState.PAUSE);
                break;
            default:
                break;
        }
    }

    public void checkCollisions(){

    }

    public void resetSurf(){
        if(!CollectionUtils.isEmpty(spriteList)) spriteList.clear();
        if(!CollectionUtils.isEmpty(npcs)) npcs.clear();
        if(!CollectionUtils.isEmpty(foes)) foes.clear();
        if(!CollectionUtils.isEmpty(bottoms)) bottoms.clear();
        if(!CollectionUtils.isEmpty(tops)) tops.clear();
        if(!CollectionUtils.isEmpty(all)) all.clear();
        if(player!=null) {player.reset();}
        lives = 3;
        boosts = 3;
    }

    public void buildStart(){
        buildScene();
    }

    public void buildScene(){
        spriteList.add(player);
        spriteList.add(bkg);
//        spriteList.addAll(Pile.Builder.buildByCustomPilesAt(CustomPiles.Start1,gridSize,0,0).objsList);
        buildPiles(CustomPiles.Start1,width/2,height/2);
        System.out.println(spriteList.size());
    }

    public void buildNextScene(){

    }


    public void buildPiles(CustomPiles piles,int cx, int cy){
        spriteList.addAll(Pile.Builder.buildByCustomPilesAt(piles,gridSize,cx,cy).objsList);
    }

    static class Objs extends Sprite {
        ObjElements element;

        int curImgId;
        int frameCnt;

        int effectImgId;
        int effectFrameCnt;


        int speedX;
        int speedY;

        @Override
        public void draw(Graphics g,Surf surf) {
            BufferedImage[] images = element.images;
            if(element.fx!=0){
                g.drawImage(element.effect.images[effectImgId],x-(element.effect.w/2),y + (element.h/2) - (element.effect.h),null );
            }
            if (element.anmi) {
                g.drawImage(images[curImgId], x - (element.w/   2), y-(element.h/2), element.w, element.h, null);
            } else {
                g.drawImage(images[0], x-(element.w/2), y-(element.h/2), element.w, element.h, null);
            }
            g.drawRect(hitbox.x, hitbox.y, hitbox.width,hitbox.height);

        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public void move(Surf surf){
            x = x + speedX - surf.player.speedX;
            y = y + speedY - surf.player.speedY;
        }

        @Override
        public void update(Surf surf) {
            move(surf);
            if(element.fps!=0){
                frameCnt++;
                if(frameCnt==(surf.gFps/this.element.fps)){
                    curImgId = (curImgId+1)%(element.images.length);
                    frameCnt=0;
                }
            }
            if(element.effect!=null && element.effect.fps!=0){
                effectFrameCnt++;
                if(effectFrameCnt==(surf.gFps/this.element.effect.fps)){
                    effectImgId = (effectImgId+1)%(element.effect.images.length);
                    effectFrameCnt = 0;
                }
            }
        }
        static class Builder {

            static Random r = new Random(System.currentTimeMillis());

            public static Objs buildByObjElements(ObjElements objType, int x, int y) {
                Objs obj = new Objs();
                if(objType.children!=null){
                    obj.element = objType.children.get(Math.abs(r.nextInt())%(objType.children.size()));
                }else{
                    obj.element = objType;
                }
                obj.x = x;
                obj.y = y;
                obj.curImgId = 0;
                obj.frameCnt=0;
                obj.effectFrameCnt = 0;
                obj.effectImgId = 0;
                obj.hitbox = new Rectangle();
                return obj;
            }
            public static Objs buildByObjElements(ObjElements objType, int x, int y, int speedX, int speedY) {
                Objs obj = buildByObjElements(objType, x, y);
                obj.speedX = speedX;
                obj.speedY = speedY;
                return obj;
            }
        }
    }

    static class Pile {
        java.util.List<Objs> objsList;
        Pile() {
            objsList = new ArrayList<>();
        }
        static class Builder {


            public static void add(Pile pile, ObjElements objType, int[][] initList, int gridSize, int x, int y) {
                for (int[] l : initList) {
                    if (l.length == 2) {
                        pile.objsList.add(Objs.Builder.buildByObjElements(objType, x + l[0]*gridSize, y + l[1]*gridSize));
                    } else if (l.length == 4) {
                        pile.objsList.add(Objs.Builder.buildByObjElements(objType, x + l[0]*gridSize, y + l[1]*gridSize, l[2], l[3]));
                    }
                }
            }
            public static Pile buildByCustomPilesAt(CustomPiles customPiles, int gridSize, int x, int y) {
                Pile pile = new Pile();
                for (Map.Entry<ObjElements,int[][]> e: customPiles.map.entrySet()) {
                    if(e.getKey().children!=null){
                        add (pile,e.getKey(), e.getValue(), gridSize,x,y);
                    }else{
                        add(pile,e.getKey(), e.getValue(),gridSize, x ,y);
                    }
                }
                return pile;
            }
        }
    }

    enum CustomPiles {

        Start1(new HashMap<ObjElements,int[][]>(){{
            put(ObjElements.SnagTall3,new int[][]{{14,21}});
            put(ObjElements.SnagTall1,new int[][]{{-14,14},{24,6},{-32,6}});
            put(ObjElements.Slow1,new int[][]{{14,10},{-14,10}});
            put(ObjElements.Bump,new int[][]{{31, 11}, {-34, 22}, {-12, 18}, {14, 18}, {28, 18}, {40, 16}, {22, 10}, {40, 8}, {-40, 4}, {-42, 10}, {-24, 18}, {-33, 12}});
            put(ObjElements.SnagPoint1,new int[][]{{-12, 27}, {8, 20}, {9, 11}, {-8, 20}, {-9, 11}, {12, 27}});
            put(ObjElements.Boost,new int[][]{{-4,2},{4,2}});
            put(ObjElements.SlowBig,new int[][]{{22, 26}, {35, 21}, {-20, 21}, {-36, 16}, {26, 2}, {-26, 3}});
            put(ObjElements.WallSe,new int[][]{{36,8},{-10, 16},{26,16},{-20, 8}});
            put(ObjElements.WallSw,new int[][]{{-26, 16},{10, 16},{-36, 8},{20, 8}});
            put(ObjElements.WallS,new int[][]{{-22, 16},{18, 16},{-24,8},{32, 8}});
            put(ObjElements.WallF,new int[][]{{-18, 16},{14,16},{22,16},{28,8},{-28,8}});
            put(ObjElements.WallN,new int[][]{{-26, 12},{26, 12},{-36, 4},{36, 4}});
            put(ObjElements.SnagB1,new int[][]{{37,12},{-32,20}});
            put(ObjElements.SnagB2,new int[][]{{15,11},{-40,12}});
            put(ObjElements.SnagB4,new int[][]{{41, 11},{-31, 13},{-22, 12},{-17, 22},{-22, 24},{36, 17},{20, 12},{-39, 18},{31, 14},{18, 21}});
        }}),


        Tiny1(new int[][]{}),
        Tiny2(new int[][]{}),
        Tiny3(new int[][]{}),
        Small1(new int[][]{
        }),
        Small2(new int[][]{
        }),
        Small3(new int[][]{
        }),
        Large1(new int[][]{}),
        Large2(new int[][]{}),
        Large3(new int[][]{});
        int[][] elements;

        Map<ObjElements,int[][]> map;

        CustomPiles(int[][] arr) {
            elements = arr;
        }

        int[][] getElements() {
            return elements;
        }

        CustomPiles(CustomPiles[] cpArr) {
        }


        CustomPiles(Map<ObjElements,int[][]> map){
            this.map = map;
        }
    }

    enum BoxElement {
        B96_64_Small(new Rectangle(20, 32, 56, 30)),
        B64_64_Small(new Rectangle(14, 32, 36, 28)),
        B128_128_Small(new Rectangle(20, 64, 88, 60)),
        B192_128_Small(new Rectangle(20, 64, 152, 60)),
        B128_64_Small(new Rectangle(20, 32, 88, 28)),
        B64_128_Small(new Rectangle(14, 80, 36, 44)),
        B32_32_Small(new Rectangle(6, 16, 20, 14)),
        B96_96_Normal(new Rectangle(0, 0, 96, 96)),
        B64_64_Normal(new Rectangle(0, 0, 64, 64)),
        B128_128_Normal(new Rectangle(0, 0, 128, 128)),
        B192_128_Normal(new Rectangle(0, 0, 192, 128)),
        B128_64_Normal(new Rectangle(0, 0, 128, 64)),
        B64_128_Normal(new Rectangle(0, 0, 64, 128)),
        B32_32_Normal(new Rectangle(0, 0, 32, 32)),
        B384_192_Normal(new Rectangle(0, 0, 384, 192)),
        B192_64_Normal(new Rectangle(0, 0, 192, 64));

        BoxElement(Rectangle r) {
        }
    }

     enum ObjElements {
        Fx(0, 96, 64, BoxElement.B96_64_Small, 0,true,6, null, ImgFactory.fx),

        SnagSide1(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.snowMan[0]}),
        SnagSide2(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.snowMan[1]}),
        SnagHurdle1(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.woods[0]}),
        SnagHurdle2(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.woods[1]}),
        SnagVehicle1(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.shrubs[0]}),
        SnagVehicle2(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.shrubs[1]}),
        SnagCommon1(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.smallTrees[0]}),
        SnagCommon2(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.smallTrees[1]}),
        SnagCommon3(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.smallTrees[2]}),
        SnagCommon4(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.smallTrees[3]}),
        SnagCommon5(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.smallTrees[4]}),
        SnagWarn(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.deadBoard}),
        SnagIsle1(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.bushes[0]}),
        SnagIsle2(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.bushes[1]}),
        SnagIsle3(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.bushes[2]}),
        SnagSpecial1(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.snagSpecial[0]}),
        SnagSpecial2(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.snagSpecial[1]}),
        SnagSpecial3(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.snagSpecial[2]}),
        SnagSpecial4(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.snagSpecial[3]}),
        SnagDebris1(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.warnBoard}),
        SnagDebris2(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.downBoard}),
        SnagBeacon1(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.flag}),
        SnagPoint1(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.redFlag}),
        SnagRare1(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.hole}),
        SnagDecor1(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.wastedSkateBoard}),
        SnagDecor2(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.wastedSingleBoard}),
        SnagDecor3(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.bin}),
        SnagDecor4(1, 64, 64, BoxElement.B64_64_Small,12,false,0, Fx, new BufferedImage[]{ImgFactory.stub}),
        //        SnagSml(1,64,64,BoxElement.B64_64_Small,new BufferedImage[]{
//            ImgFactory.warnBoard,ImgFactory.downBoard,ImgFactory.flag,ImgFactory.redFlag,
//                ImgFactory.hole,ImgFactory.wastedSkateBoard,ImgFactory.wastedSingleBoard,
//                ImgFactory.bin,ImgFactory.stub
//        }),
        SnagTall1(1, 64, 128, BoxElement.B64_128_Small,36,false,0, Fx, new BufferedImage[]{ImgFactory.snagTall[0]}),
        SnagTall2(1, 64, 128, BoxElement.B64_128_Small,36,false,0, Fx, new BufferedImage[]{ImgFactory.snagTall[1]}),
        SnagTall3(1, 64, 128, BoxElement.B64_128_Small,36,false,0, Fx, new BufferedImage[]{ImgFactory.snagTall[2]}),
        SnagTall4(1, 64, 128, BoxElement.B64_128_Small,36,false,0, Fx, new BufferedImage[]{ImgFactory.snagTall[3]}),
        SnagTall5(1, 64, 128, BoxElement.B64_128_Small,36,false,0, Fx, new BufferedImage[]{ImgFactory.snagTall[4]}),
        SnagTall6(1, 64, 128, BoxElement.B64_128_Small,36,false,0, Fx, new BufferedImage[]{ImgFactory.snagTall[5]}),
        SnagTall7(1, 64, 128, BoxElement.B64_128_Small,36,false,0, Fx, new BufferedImage[]{ImgFactory.snagTall[6]}),
        //        SnagTall(1,64,128,BoxElement.B64_128_Small,ImgFactory.snagTall),
        SnagTall(SnagTall1, SnagTall2, SnagTall3, SnagTall4, SnagTall5, SnagTall6, SnagTall7),
        SnagB1(SnagSide1,SnagSide2),
        SnagB2(SnagHurdle1,SnagHurdle2),
        SnagB3(SnagVehicle1,SnagVehicle2),
        SnagB4(SnagCommon1,SnagCommon2,SnagCommon3,SnagCommon4,SnagCommon5),
        SnagB5(SnagWarn),
        SnagB6(SnagIsle1,SnagIsle2,SnagIsle3),
        SnagB7(SnagSpecial1,SnagSpecial2,SnagSpecial3,SnagSpecial4),

        Spin1(1, 32, 32, BoxElement.B32_32_Normal,4,false,0,Fx, new BufferedImage[]{ImgFactory.spin[0]}),
        Spin2(1, 32, 32, BoxElement.B32_32_Normal,4,false,0,Fx, new BufferedImage[]{ImgFactory.spin[1]}),
        Spin3(1, 32, 32, BoxElement.B32_32_Normal,4,false,0,Fx, new BufferedImage[]{ImgFactory.spin[2]}),
        Spin4(1, 32, 32, BoxElement.B32_32_Normal,4,false,0,Fx, new BufferedImage[]{ImgFactory.spin[3]}),
        Spin5(1, 32, 32, BoxElement.B32_32_Normal,4,false,0,Fx, new BufferedImage[]{ImgFactory.spin[4]}),
        Spin(Spin1, Spin2, Spin3, Spin4, Spin5),
        //        Spin(1,32,32,BoxElement.B32_32_Normal,ImgFactory.spin),
        Sprial(1, 128, 128, BoxElement.B128_128_Normal, 0,true, 6,null, ImgFactory.lakes),
        Block1(1, 128, 128, BoxElement.B128_128_Small,0,false,0, null, new BufferedImage[]{ImgFactory.smallRocks[0]}),
        Block2(1, 128, 128, BoxElement.B128_128_Small,0,false,0, null, new BufferedImage[]{ImgFactory.smallRocks[1]}),
        Block3(1, 128, 128, BoxElement.B128_128_Small,0,false,0, null, new BufferedImage[]{ImgFactory.smallRocks[2]}),
        Block(Block1, Block2, Block3),
        BlockBig1(1, 192, 128, BoxElement.B192_128_Small,0,false,0, null, new BufferedImage[]{ImgFactory.bigRocks[0]}),
        BlockBig2(1, 192, 128, BoxElement.B192_128_Small,0,false,0, null, new BufferedImage[]{ImgFactory.bigHouse}),
        BlockBig3(1, 192, 128, BoxElement.B192_128_Small,0,false,0, null, new BufferedImage[]{ImgFactory.bigRocks[1]}),

        BlockBig(BlockBig1, BlockBig2, BlockBig3),
        MarkerPass(1, 64, 64, BoxElement.B64_64_Small,12,false,0,Fx, new BufferedImage[]{ImgFactory.marker[0]}),
        MarkerFail(1, 64, 64, BoxElement.B64_64_Small,12,false,0,Fx,  new BufferedImage[]{ImgFactory.marker[1]}),
        Marker(MarkerPass, MarkerFail),
        GuidePass(1, 32, 32, BoxElement.B32_32_Normal, 4,false,0,Fx,new BufferedImage[]{ImgFactory.guide[0]}),
        GuideFail(1, 32, 32, BoxElement.B32_32_Normal,4,false,0,Fx, new BufferedImage[]{ImgFactory.guide[1]}),
        Guide(GuidePass, GuideFail),
        //        Block(1,128,128,BoxElement.B128_128_Small,ImgFactory.smallRocks),
//        BlockBig(1,192,128,BoxElement.B192_128_Small,ImgFactory.bigRocks),
//        Marker(1,64,64,BoxElement.B64_64_Small,ImgFactory.marker),
//        Guide(1,32,32,BoxElement.B32_32_Normal,ImgFactory.guide),
        Slow1(1, 64, 64, BoxElement.B64_64_Normal,0, true,6,null, ImgFactory.slow1),
        Slow2(1, 64, 64, BoxElement.B64_64_Normal,0, true,6,null, ImgFactory.slow2),
        Slow3(1, 64, 64, BoxElement.B64_64_Normal,0, true,6,null, ImgFactory.slow3),
        Slow(Slow1, Slow2, Slow3),
        SlowBig(1, 192, 64, BoxElement.B192_64_Normal,0, true,6,null, ImgFactory.slowBig),
        Bump1(1, 64, 64, BoxElement.B64_64_Normal, 0, true,6,null, ImgFactory.bump1),
        Bump2(1, 64, 64, BoxElement.B64_64_Normal, 0, true,6,null, ImgFactory.bump2),
        Bump3(1, 64, 64, BoxElement.B64_64_Normal, 0, true,6,null, ImgFactory.bump3),
        Bump(Bump1, Bump2, Bump3),
        BumpBig(1, 192, 64, BoxElement.B192_64_Normal,0, true,6,null, ImgFactory.bumpBig),
        Ramp(1, 64, 64, BoxElement.B64_64_Normal,12, true,6,Fx, ImgFactory.ramp),
        Boost(1, 64, 64, BoxElement.B64_64_Normal,12, true,6,Fx, ImgFactory.boost),
        Life(1, 64, 64, BoxElement.B64_64_Normal,12, true,6,Fx, ImgFactory.bigLife),
        Coin(1, 64, 64, BoxElement.B64_64_Normal,12, true,6,Fx, ImgFactory.bigCoin),
        FriendNormal(1, 64, 64, BoxElement.B64_64_Normal,0, true,6,null, ImgFactory.friend),
        FriendCrash(1, 64, 64, BoxElement.B64_64_Normal,0,false,0,null, new BufferedImage[]{ImgFactory.friend[0]}),
        Friend(FriendNormal, FriendCrash),
        Lure(1, 64, 64, BoxElement.B64_64_Normal,12, true,6, Fx,ImgFactory.lure),
        Ambient1(1, 64, 64, BoxElement.B64_64_Normal,12, true,6, Fx, ImgFactory.ambient1),
        Ambient2(1, 64, 64, BoxElement.B64_64_Normal,12, true,6, Fx, ImgFactory.ambient2),
        Ambient3(1, 64, 64, BoxElement.B64_64_Normal,12, true,6, Fx, ImgFactory.ambient3),
        Ambient(Ambient1, Ambient2, Ambient3),
        Finish(1, 384, 192, BoxElement.B384_192_Normal,0,false,0,null, new BufferedImage[]{ImgFactory.finish}),
        CheckoutPoint(1, 384, 192, BoxElement.B384_192_Normal,0,false,0,null, new BufferedImage[]{ImgFactory.finish}),
        //        Wall(1,64,64,BoxElement.B64_64_Small,ImgFactory.wall),
        WallSw(1, 64, 64, BoxElement.B64_64_Small,12,false,0,Fx, new BufferedImage[]{ImgFactory.wall[0]}),
        WallS(1, 64, 64, BoxElement.B64_64_Small,12,false,0,Fx, new BufferedImage[]{ImgFactory.wall[1]}),
        WallSe(1, 64, 64, BoxElement.B64_64_Small, 12,false,0,Fx,new BufferedImage[]{ImgFactory.wall[2]}),
        WallW(1, 64, 64, BoxElement.B64_64_Small,12,false,0,Fx, new BufferedImage[]{ImgFactory.wall[3]}),
        WallE(1, 64, 64, BoxElement.B64_64_Small, 12,false,0,Fx,new BufferedImage[]{ImgFactory.wall[4]}),
        WallN(1, 64, 64, BoxElement.B64_64_Small,12,false,0,Fx, new BufferedImage[]{ImgFactory.wall[5]}),
        WallB(1, 64, 64, BoxElement.B64_64_Small, 12,false,0,Fx,new BufferedImage[]{ImgFactory.wall[6]}),
        WallF1(1, 64, 64, BoxElement.B64_64_Small,12,false,0,Fx, new BufferedImage[]{ImgFactory.wall[7]}),
        WallF2(1, 64, 64, BoxElement.B64_64_Small,12,false,0,Fx, new BufferedImage[]{ImgFactory.wall[8]}),
        WallF3(1, 64, 64, BoxElement.B64_64_Small, 12,false,0,Fx,new BufferedImage[]{ImgFactory.wall[9]}),
        Wall(WallSw, WallS, WallSe, WallW, WallE, WallN, WallB, WallF1, WallF2, WallF3),
        WallF(WallF1,WallF2,WallF3),
        WallDecorA(1, 32, 32, BoxElement.B32_32_Normal,0,false,0,null, new BufferedImage[]{ImgFactory.wallDecor[0]}),
        WallDecorB(1, 32, 32, BoxElement.B32_32_Normal,0,false,0,null, new BufferedImage[]{ImgFactory.wallDecor[1]}),
        WallDecorC(1, 32, 32, BoxElement.B32_32_Normal,0,false,0,null, new BufferedImage[]{ImgFactory.wallDecor[2]}),
        WallDecorD(1, 32, 32, BoxElement.B32_32_Normal,0,false,0,null, new BufferedImage[]{ImgFactory.wallDecor[3]}),
        WallDecorE(1, 32, 32, BoxElement.B32_32_Normal,0,false,0,null, new BufferedImage[]{ImgFactory.wallDecor[4]}),
        WallDecorF(1, 32, 32, BoxElement.B32_32_Normal,0,false,0,null, new BufferedImage[]{ImgFactory.wallDecor[5]}),
        WallDecorG(1, 32, 32, BoxElement.B32_32_Normal,0,false,0,null, new BufferedImage[]{ImgFactory.wallDecor[6]}),
        WallDecorH(1, 32, 32, BoxElement.B32_32_Normal,0,false,0,null, new BufferedImage[]{ImgFactory.wallDecor[7]}),
        //        WallDecor(1,32,32,BoxElement.B32_32_Normal,ImgFactory.wallDecor),
        WallDecor(WallDecorA, WallDecorB, WallDecorC, WallDecorD, WallDecorE, WallDecorF, WallDecorG),
        NpcLeft(1, 64, 64, BoxElement.B64_64_Small,0,false,0,null, ImgFactory.npcLeft),
        NpcRight(1, 64, 64, BoxElement.B64_64_Small,0,false,0,null, ImgFactory.npcRight),
        NpcCrash(1, 64, 64, BoxElement.B64_64_Small,0,false,0,null, ImgFactory.npcCrash),

        FoeChase(1, 128, 128, BoxElement.B128_128_Normal,0, true,6,null, ImgFactory.foeChase),
        FoeCrash(1, 128, 128, BoxElement.B128_128_Normal, 0, true,6,null, ImgFactory.foeCrash),
        FoeEnd(1, 128, 128, BoxElement.B128_128_Normal, 0, true,6,null, ImgFactory.foeEnd),
        Snag(SnagSide1, SnagSide2, SnagHurdle1, SnagHurdle2, SnagVehicle1, SnagVehicle2,
                SnagCommon1, SnagCommon2, SnagCommon3, SnagCommon4, SnagCommon5, SnagWarn,
                SnagIsle1, SnagIsle2, SnagIsle3, SnagSpecial1, SnagSpecial2, SnagSpecial3, SnagSpecial4),

        SnagSml(SnagDebris1, SnagDebris2, SnagBeacon1, SnagPoint1, SnagRare1, SnagDecor1, SnagDecor2,
                SnagDecor3, SnagDecor4);
        int id, w, h,fx,fps;
        boolean anmi;
        java.util.List<ObjElements> children;
        BoxElement boxType;
        ObjElements effect;
        BufferedImage[] images;
//        ObjElements(int id, int w, int h, BoxElement boxType, BufferedImage[] bufferedImages) {
//            this.id = id;
//            this.w = w;
//            this.h = h;
//            this.boxType = boxType;
//            this.images = bufferedImages;
//        }

//        ObjElements(int id, int w, int h, BoxElement boxType, int fx, BufferedImage[] bufferedImages){
//            this.id = id;
//            this.w = w;
//            this.h = h;
//            this.boxType = boxType;
//            this.fx = fx;
//            this.images = bufferedImages;
//        }

        ObjElements(int id, int w, int h, BoxElement boxType, int fx,boolean anmi, int fps, ObjElements effect, BufferedImage[] bufferedImages){
            this.id = id;
            this.w = w;
            this.h = h;
            this.boxType = boxType;
            this.fx = fx;
            this.fps = fps;
            this.anmi = anmi;
            this.images = bufferedImages;
            this.effect = effect;
        }
        //        ObjElements(int id, int w, int h, BoxElement boxType, boolean anmi,int fps, BufferedImage[] bufferedImages) {
//            this.id = id;
//            this.w = w;
//            this.h = h;
//            this.boxType = boxType;
//            this.fps = fps;
//            this.anmi = anmi;
//            this.images = bufferedImages;
//        }
        ObjElements(ObjElements... e) {
            this.children = Arrays.stream(e).collect(Collectors.toList());
        }
    }

    static class ImgFactory {
        static BufferedImage bkgImg;//
        // = ImageIO.read(new FileInputStream(ImageIO.read(new FileInputStream("C:\\Users\\chenj\\IdeaProjects\\mygw\\src\\test\\java\\com\\example\\mygw\\bg.png"))));
        static BufferedImage playerImage;
        static BufferedImage objectsImage;
        static BufferedImage[][] playerImageArr;
        static BufferedImage[] dogImageArr;
        static BufferedImage[][] skateBoardImageArr;
        static BufferedImage[] hearts;
        static BufferedImage[] lights;
        static BufferedImage defend;
        static BufferedImage infinite;
        static BufferedImage smallCoin;
        static BufferedImage warnBoard;
        static BufferedImage downBoard;
        static BufferedImage flag;
        static BufferedImage redFlag;
        static BufferedImage hole;
        static BufferedImage wastedSkateBoard;
        static BufferedImage wastedSingleBoard;
        static BufferedImage bin;
        static BufferedImage stub;
        static BufferedImage[] snowMan;
        static BufferedImage[] woods;
        static BufferedImage[] shrubs;
        static BufferedImage[] smallTrees;
        static BufferedImage deadBoard;
        static BufferedImage[] bushes;
        static BufferedImage[] lakes;
        static BufferedImage[] bigRocks;
        static BufferedImage bigHouse;
        static BufferedImage[] smallRocks;
        static BufferedImage[] bigTrees;
        static BufferedImage[] fx;
        static BufferedImage[] ramp;
        static BufferedImage[] boost;
        static BufferedImage[] bigLife;
        static BufferedImage[] bigCoin;
        static BufferedImage[] friend;
        static BufferedImage[] ambient1, ambient2, ambient3;
        static BufferedImage finish;
        static BufferedImage[] lure;
        static BufferedImage[] npcLeft, npcRight, npcCrash;
        static BufferedImage[] foeChase, foeCrash, foeEnd;
        static BufferedImage[] marker;
        static BufferedImage[] slow1, slow2, slow3;
        static BufferedImage[] slowBig;
        static BufferedImage[] bump1, bump2, bump3;
        static BufferedImage[] bumpBig;
        static BufferedImage[] snagSpecial;
        static BufferedImage[] snagTall;
        static BufferedImage[] spin;
        static BufferedImage[] guide;
        static BufferedImage[] wallDecor;
        static BufferedImage[] wall;

        public ImgFactory() {
            try {
                bkgImg = ImageIO.read(new FileInputStream("C:\\Users\\chenj\\IdeaProjects\\mygw\\src\\test\\java\\com\\example\\mygw\\bg.png"));
                playerImage = ImageIO.read(new FileInputStream("C:\\Users\\chenj\\IdeaProjects\\mygw\\src\\test\\java\\com\\example\\mygw\\player.png"));
                objectsImage = ImageIO.read(new FileInputStream("C:\\Users\\chenj\\IdeaProjects\\mygw\\src\\test\\java\\com\\example\\mygw\\objects.png"));
                initPlayerImage();
                initObjectsImage();
                initDogImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void initPlayerImage() {
            playerImageArr = new BufferedImage[9][10];
            int sx = 0, sy = 256;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 10; j++) {
                    playerImageArr[i][j] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D sk_g = (Graphics2D) (playerImageArr[i][j].getGraphics());
                    sk_g.drawImage(playerImage.getSubimage(sx + j * 64, sy + i * 64, 64, 64), 0, 0, 64, 64, null);
                    sk_g.dispose();
                }
            }
            sx = sy = 0;
            skateBoardImageArr = new BufferedImage[10][3];
            for (int i = 0; i < skateBoardImageArr.length; i++) {
                for (int j = 0; j < skateBoardImageArr[0].length; j++) {
                    skateBoardImageArr[i][j] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D sk_g = (Graphics2D) (skateBoardImageArr[i][j].getGraphics());
                    sk_g.drawImage(playerImage.getSubimage(sx + i * 64, sy + j * 64, 64, 64), 0, 0, 64, 64, null);
                    sk_g.dispose();
                }
            }
        }

        public void initDogImage() {
        }

        public void initObjectsImage() {
            initHearts();
            initLights();
            initDefend();
            initInfinite();
            initSmallCoin();
            initWarnBoard();
            initDownBoard();
            initFlag();
            initRedFlag();
            initHole();
            initWastedSkateBoard();
            initWastedSingleBoard();
            initBin();
            initStub();
            initSnowMan();
            initWoods();
            initShrubs();
            initSmallTrees();
            initDeadBoard();
            initBushes();
            initLakes();
            initBigRocks();
            initBigHouse();
            initSmallRocks();
            initBigTrees();
            initFx();
            initRamp();
            initBoost();
            initBigLife();
            initBigCoin();
            initFriend();
            initAmbient();
            initFinish();
            initLure();
            initNpc();
            initFoe();
            initMarker();
            initSlow();
            initSlowBig();
            initBump();
            initBumpBig();
            initSnagSpecial();
            initSnagTall();
            initSpin();
            initGuide();
            initWallDecor();
            initWall();
        }

        public void initHearts() {
            hearts = new BufferedImage[2];
            hearts[0] = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
            hearts[1] = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, hearts[0], 0, 0, 24, 24);
            copy(objectsImage, hearts[1], 0, 24, 24, 24);
        }

        public void initLights() {
            lights = new BufferedImage[2];
            lights[0] = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
            lights[1] = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, lights[0], 24, 0, 24, 24);
            copy(objectsImage, lights[1], 24, 24, 24, 24);
        }

        public void initDefend() {
            defend = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, defend, 48, 0, 24, 24);
        }

        public void initInfinite() {
            infinite = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, infinite, 48, 24, 24, 24);
        }

        public void initSmallCoin() {
            smallCoin = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, smallCoin, 72, 0, 24, 24);
        }

        public void initFx() {
            fx = new BufferedImage[3];
            fx[0] = new BufferedImage(96, 64, BufferedImage.TYPE_INT_ARGB);
            fx[1] = new BufferedImage(96, 64, BufferedImage.TYPE_INT_ARGB);
            fx[2] = new BufferedImage(96, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, fx[0], 96, 0, 96, 64);
            copy(objectsImage, fx[1], 96 * 2, 0, 96, 64);
            copy(objectsImage, fx[2], 96 * 3, 0, 96, 64);
        }

        public void initWarnBoard() {
            warnBoard = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, warnBoard, 6 * 64, 0, 64, 64);
        }

        public void initDownBoard() {
            downBoard = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, downBoard, 7 * 64, 0, 64, 64);
        }

        public void initFlag() {
            flag = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, flag, 8 * 64, 0, 64, 64);
        }

        public void initRedFlag() {
            redFlag = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, redFlag, 9 * 64, 0, 64, 64);
        }

        public void initHole() {
            hole = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, hole, 10 * 64, 0, 64, 64);
        }

        public void initWastedSkateBoard() {
            wastedSkateBoard = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, wastedSkateBoard, 11 * 64, 0, 64, 64);
        }

        public void initWastedSingleBoard() {
            wastedSingleBoard = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, wastedSingleBoard, 12 * 64, 0, 64, 64);
        }

        public void initBin() {
            bin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, bin, 13 * 64, 0, 64, 64);
        }

        public void initStub() {
            stub = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, stub, 14 * 64, 0, 64, 64);
        }

        public void initSnowMan() {
            snowMan = new BufferedImage[2];
            snowMan[0] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            snowMan[1] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, snowMan[0], 0, 64, 64, 64);
            copy(objectsImage, snowMan[1], 64, 64, 64, 64);
        }

        public void initWoods() {
            woods = new BufferedImage[2];
            woods[0] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            woods[1] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, woods[0], 2 * 64, 64, 64, 64);
            copy(objectsImage, woods[1], 3 * 64, 64, 64, 64);
        }

        public void initShrubs() {
            shrubs = new BufferedImage[2];
            shrubs[0] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            shrubs[1] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, shrubs[0], 4 * 64, 64, 64, 64);
            copy(objectsImage, shrubs[1], 5 * 64, 64, 64, 64);
        }

        public void initSmallTrees() {
            smallTrees = new BufferedImage[5];
            for (int i = 0; i < smallTrees.length; i++) {
                smallTrees[i] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                copy(objectsImage, smallTrees[i], (6 + i) * 64, 64, 64, 64);
            }
        }

        public void initDeadBoard() {
            deadBoard = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, deadBoard, 11 * 64, 64, 64, 64);
        }

        public void initBushes() {
            bushes = new BufferedImage[3];
            for (int i = 0; i < bushes.length; i++) {
                bushes[i] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                copy(objectsImage, bushes[i], (12 + i) * 64, 64, 64, 64);
            }
        }

        public void initLakes() {
            lakes = new BufferedImage[3];
            for (int i = 0; i < lakes.length; i++) {
                lakes[i] = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                copy(objectsImage, lakes[i], (0 + i) * 128, 128, 128, 128);
            }
        }

        public void initBigRocks() {
            bigRocks = new BufferedImage[2];
            bigRocks[0] = new BufferedImage(64 * 3, 64 * 2, BufferedImage.TYPE_INT_ARGB);
            bigRocks[1] = new BufferedImage(64 * 3, 64 * 2, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, bigRocks[0], 3 * 128, 128, 192, 128);
            copy(objectsImage, bigRocks[0], 3 * 128 + 192 * 2, 128, 192, 128);
        }

        public void initBigHouse() {
            bigHouse = new BufferedImage(64 * 3, 64 * 2, BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage, bigHouse, 3 * 128 + 192, 128, 192, 128);
        }

        public void initSmallRocks() {
            smallRocks = new BufferedImage[3];
            for (int i = 0; i < smallRocks.length; i++) {
                smallRocks[i] = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                copy(objectsImage, smallRocks[i], (3 + i) * 128, 256, 128, 128);
            }
        }

        public void initBigTrees() {
            bigTrees = new BufferedImage[5];
            for (int i = 0; i < bigTrees.length; i++) {
                bigTrees[i] = new BufferedImage(64, 128, BufferedImage.TYPE_INT_ARGB);
            }
            copy(objectsImage, bigTrees[0], 12 * 64, 256, 64, 128);
            copy(objectsImage, bigTrees[1], 13 * 64, 256, 64, 128);
            copy(objectsImage, bigTrees[2], 14 * 64, 256, 64, 128);
            copy(objectsImage, bigTrees[3], 13 * 64, 384, 64, 128);
            copy(objectsImage, bigTrees[4], 14 * 64, 384, 64, 128);
        }

        public void initRamp() {
            ramp = new BufferedImage[4];
            for (int i = 0; i < 4; i++) {
                ramp[i] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            }
            copy(objectsImage, ramp[0], 960, 0, 64, 64);
            copy(objectsImage, ramp[1], 960, 64, 64, 64);
            copy(objectsImage, ramp[2], 960, 128, 64, 64);
            copy(objectsImage, ramp[3], 960, 192, 64, 64);
        }

        public void initBoost() {
            boost = new BufferedImage[4];
            for (int i = 0; i < 4; i++) {
                boost[i] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            }
            copy(objectsImage, boost[0], 1024, 0, 64, 64);
            copy(objectsImage, boost[1], 1024, 64, 64, 64);
            copy(objectsImage, boost[2], 1024, 128, 64, 64);
            copy(objectsImage, boost[3], 1024, 192, 64, 64);
        }

        public void initBigLife() {
            bigLife = new BufferedImage[4];
            for (int i = 0; i < 4; i++) {
                bigLife[i] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            }
            copy(objectsImage, bigLife[0], 1080, 0, 64, 64);
            copy(objectsImage, bigLife[0], 1080, 64, 64, 64);
            copy(objectsImage, bigLife[0], 1080, 128, 64, 64);
            copy(objectsImage, bigLife[0], 1080, 192, 64, 64);
        }

        public void initBigCoin() {
            bigCoin =  initImgHelper(objectsImage, 64, 64, new int[][]{{960, 256}, {960, 320}, {960, 384}, {960, 448}});
        }

        public void initFriend() {
            friend = initImgHelper(objectsImage, 64, 64, new int[][]{{1024, 256}, {1024, 320}, {1024, 384}, {1024, 448}});
        }

        public void initAmbient() {
            ambient1 = initImgHelper(objectsImage, 64, 64, new int[][]{{1344, 0}, {1344, 64}, {1344, 128}, {1344, 192}});
            ambient2 = initImgHelper(objectsImage, 64, 64, new int[][]{{1408, 0}, {1408, 64}, {1408, 128}, {1408, 192}});
            ambient3 = initImgHelper(objectsImage, 64, 64, new int[][]{{1472, 0}, {1472, 64}, {1472, 128}, {1472, 192}});
        }

        public void initFinish() {
            finish = initImgHelper(objectsImage, 384, 192, new int[]{0, 256});
        }

        public void initLure() {
            lure = initImgHelper(objectsImage, 64, 64, new int[][]{{1088, 256}, {1088, 320}, {1088, 384}, {1088, 448}});
        }

        public void initNpc() {
            npcLeft = initImgHelper(objectsImage, 64, 64, new int[][]{{1152, 0}, {1152, 64}, {1152, 128}, {1152, 192}});
            npcRight = initImgHelper(objectsImage, 64, 64, new int[][]{{1216, 0}, {1216, 64}, {1216, 128}, {1216, 192}});
            npcCrash = initImgHelper(objectsImage, 64, 64, new int[][]{{1280, 0}, {1280, 64}, {1280, 128}, {1280, 192}});
        }

        public void initFoe() {
            foeChase = initImgHelper(objectsImage, 128, 128, new int[][]{{1152, 256}, {1280, 256}, {1408, 256}, {1536, 256}});
            foeCrash = initImgHelper(objectsImage, 128, 128, new int[][]{{1664, 256}, {1792, 256}});
            foeEnd = initImgHelper(objectsImage, 128, 128, new int[][]{{1152, 384}, {1280, 384}, {1408, 384}, {1536, 384}, {1664, 384}, {1792, 384}});
        }

        public void initMarker() {
            marker = initImgHelper(objectsImage, 64, 64, new int[][]{{1856, 192}, {1792, 192}});
        }

        public void initSlow() {
            slow1 = initImgHelper(objectsImage, 64, 64, new int[][]{{1536, 0}, {1536, 64}, {1536, 128}});
            slow2 = initImgHelper(objectsImage, 64, 64, new int[][]{{1600, 0}, {1600, 64}, {1600, 128}});
            slow3 = initImgHelper(objectsImage, 64, 64, new int[][]{{1664, 0}, {1664, 64}, {1664, 128}});
        }

        public void initSlowBig() {
            slowBig = initImgHelper(objectsImage, 192, 64, new int[][]{{1536, 0}, {1536, 64}, {1536, 128}});
        }

        public void initBump() {
            bump1 = initImgHelper(objectsImage, 64, 64, new int[][]{{1728, 0}, {1728, 64}, {1728, 128}});
            bump2 = initImgHelper(objectsImage, 64, 64, new int[][]{{1792, 0}, {1792, 64}, {1792, 128}});
            bump3 = initImgHelper(objectsImage, 64, 64, new int[][]{{1856, 0}, {1856, 64}, {1856, 128}});
        }

        public void initBumpBig() {
            bumpBig = initImgHelper(objectsImage, 192, 64, new int[][]{{1728, 0}, {1728, 64}, {1728, 128}});
        }

        public void initSnagSpecial() {
            snagSpecial = initImgHelper(objectsImage, 64, 64, new int[][]{{1536, 192}, {1600, 192}, {1664, 192}, {1728, 192}});
        }

        public void initSnagTall() {
            snagTall = initImgHelper(objectsImage, 64, 128, new int[][]{{640, 384}, {704, 384}, {768, 384}, {832, 384}, {896, 384},
                    {768, 256}, {832, 256}, {896, 256}});
        }

        public void initSpin() {
            spin = initImgHelper(objectsImage, 32, 32, new int[][]{{384, 384}, {416, 384}, {448, 384}, {480, 384}, {512, 384}});
        }

        public void initGuide() {
            guide = initImgHelper(objectsImage, 32, 32, new int[][]{{544, 384}, {576, 384}, {608, 384}});
        }

        public void initWallDecor() {
            wallDecor = initImgHelper(objectsImage, 32, 32, new int[][]{{384, 416}, {416, 416}, {448, 416}, {480, 416}, {512, 416}, {544, 416}, {576, 416}, {608, 416}});
        }

        public void initWall() {
            wall = initImgHelper(objectsImage, 64, 64, new int[][]{{0, 448}, {64, 448}, {128, 448}, {192, 448},
                    {256, 448}, {320, 448}, {384, 448}, {448, 448}, {512, 448}, {576, 448}});
        }

        public BufferedImage[] initImgHelper(BufferedImage source, int w, int h, int[][] pos) {
            int s = pos.length;
            BufferedImage[] destArr = new BufferedImage[s];
            for (int i = 0; i < destArr.length; i++) {
                destArr[i] = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                copy(source, destArr[i], pos[i][0], pos[i][1], w, h);
            }
            return destArr;
        }

        public BufferedImage initImgHelper(BufferedImage source,  int w, int h, int[] pos) {
            BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            copy(source, dest, pos[0], pos[1], w, h);
            return dest;
        }

        public static void copy(BufferedImage source, BufferedImage dest, int sx, int sy, int width, int height) {
            Graphics2D g = (Graphics2D) dest.getGraphics();
            g.drawImage(source.getSubimage(sx, sy, width, height), 0, 0, width, height, null);
            g.dispose();
        }
    }

    public static void main(String[] args) {
        Surf surf = new Surf();
        surf.loop();
        System.out.println(Thread.currentThread().getName());
        System.out.println(ObjElements.Fx);

        System.out.println(CustomPiles.Start1);
//        surf.loop();
    }
}
