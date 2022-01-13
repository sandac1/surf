package com.example.mygw;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class Surf {
    SurfFrame surfFrame;
    Bkg bkg;
    Player player;
    SurfState surfState;
    int width;
    int height;
    BufferedImage canvasImage;
    Graphics canvasGraphics;
    java.util.List<Sprite> spriteList;

    ImgFactory imgFactory;


    public Surf() {
        try {
            init();
        } catch (Exception e) {
        }
    }

    public void init() throws IOException {
        width = 600;
        height = 600;
        spriteList = new ArrayList<>();

        imgFactory = new ImgFactory();
        bkg = new Bkg(width, height, this);
        spriteList.add(bkg);
        surfState = SurfState.RUNNING;
        player = new Player(width, height, this);
        spriteList.add(player);
        canvasImage = new BufferedImage(width , height , BufferedImage.TYPE_INT_ARGB);
        canvasGraphics = canvasImage.getGraphics();
        surfFrame = new SurfFrame(this);
        surfFrame.setTitle("surf");
        surfFrame.setVisible(true);
        surfFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        surfFrame.surfPanel.setSize(width, height);
        surfFrame.surfPanel.setLayout(null);
        surfFrame.setSize(new Dimension(width+20, height+30));
    }

    static class SurfFrame extends JFrame {
        Surf surf;
        private final SurfPanel surfPanel;

        public SurfFrame(Surf s) {
            surf = s;
            surfPanel = new SurfPanel(surf);
            this.add(surfPanel);
            surfPanel.setFocusable(true);
        }
    }

    static class SurfPanel extends JPanel {
        Surf surf;

        public SurfPanel(Surf s) {
            surf = s;
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    surf.processMouse(e);
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
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.drawImage(surf.canvasImage, 0, 0, surf.width, surf.height, null);
        }
    }

    static class Player implements Sprite {
        int x;
        int y;
        int speedX;
        int speedY;
        State state;
        RunState runState;

        BufferedImage[][] skateBoardImageArr;
        BufferedImage[][] playerImgArr;

        Rectangle box;

        int roleId;

        int skId;
        Surf surf;

        static enum State {READY, RUNNING, DIE;}

        static enum RunState {STOP, LEFT_45, LEFT_30, FRONT, RIGHT_30, RIGHT_45, SLIPPED,SIT,JUMP_DOWN,JUMP_UP;}

        public Player() throws IOException {
        }

        public Player(int w, int h, Surf s) throws IOException {
            x = w / 2 - 32;
            y = h / 2 - 32;
            state = State.READY;
            runState = RunState.FRONT;
            speedX = 0;
            speedY = 10;
            surf = s;
            box = new Rectangle(x + 32 - 10, y + 32, 20, 20);
            skateBoardImageArr = surf.imgFactory.skateBoardImageArr;
            playerImgArr = surf.imgFactory.playerImageArr;
            skId = 0;
            roleId = 0;
        }

        public void setRunState(RunState runState) {
            this.runState = runState;
            switch (runState) {
                case STOP:{
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
        public void draw(Graphics g) {
            g.drawImage(skateBoardImageArr[runState.ordinal()][skId], this.x , this.y, 64, 64, null);
            g.drawImage(playerImgArr[roleId][runState.ordinal()], this.x , this.y, 64, 64, null);
            g.setColor(Color.RED);
            g.drawRect(box.x, box.y, box.width, box.height);
            skId = (skId + 1) % 3;
        }

        @Override
        public int getOrder() {
            return y + 64;
        }

        @Override
        public void update() {
        }
    }

    static enum SurfState {READY, RUNNING, PAUSE, OVER;}

    static class Bkg implements Sprite {
        BufferedImage blockImg;
        int width, height;
        int imgWidth, imgHeight;
        Surf surf;
        int x, y;

        public Bkg(int w, int h, Surf s) {
            try {
                blockImg = ImageIO.read(new FileInputStream("C:\\Users\\chenj\\IdeaProjects\\mygw\\src\\test\\java\\com\\example\\mygw\\bg.png"));
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
        public void draw(Graphics g) {
            int xc = (surf.canvasImage.getWidth()-x)/imgWidth + 1;
            int yc = (surf.canvasImage.getHeight()-y)/imgWidth + 1;
            for (int i = 0; i < xc; i++) {
                for (int j = 0; j < yc; j++) {
                    g.drawImage(blockImg, x+i * imgWidth, y+j * imgHeight, imgWidth, imgHeight, null);
                    g.drawRect(x+i*imgWidth,y+j*imgHeight,imgWidth,imgHeight);
                }
            }
        }

        @Override
        public int getOrder() {
            return Integer.MIN_VALUE;
        }

        @Override
        public void update() {
            x = x - surf.player.speedX;
            y = y - surf.player.speedY;
            if(x>0){
                x = x - imgWidth;
            }
            if(x< -imgWidth){
                x = x+imgWidth;
            }
            if(y< -imgHeight){
                y = y+imgHeight;
                System.out.println("y:"+y);
            }
        }
    }

    static class Obstacle implements Sprite{

        enum ObstacleType{

        }

        @Override
        public void draw(Graphics g) {

        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public void update() {

        }
    }


    static interface Sprite {
        void draw(Graphics g);

        int getOrder();

        void update();
    }

    public void run() {
        while (true) {
            try {
                update();
                draw();
                this.surfFrame.surfPanel.repaint();
                Thread.sleep(30);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void draw() {
        Graphics2D g = (Graphics2D) canvasImage.getGraphics();
        g.setBackground(Color.LIGHT_GRAY);
        g.clearRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight());
        spriteList.sort(Comparator.comparing(Sprite::getOrder));
        spriteList.forEach(a -> a.draw(g));
        g.dispose();
    }


    public void update() {
        spriteList.forEach(Sprite::update);
    }

    public void processMouse(MouseEvent e) {
        int xx = e.getX();
        int yy = e.getY();
        if (surfState == SurfState.RUNNING) {
            if(yy<height/2){
                player.setRunState(Player.RunState.STOP);
            }else{
                int area = (xx / (width / 5)) + 1;
                player.setRunState(Player.RunState.values()[area]);

            }
        }
    }

    public void reLocateX(int x) {
        if (x == 0) {
        }
    }


    static class ImgFactory{

        BufferedImage bkgImg ;//
        // = ImageIO.read(new FileInputStream(ImageIO.read(new FileInputStream("C:\\Users\\chenj\\IdeaProjects\\mygw\\src\\test\\java\\com\\example\\mygw\\bg.png"))));
        BufferedImage playerImage;

        BufferedImage objectsImage;

        BufferedImage[][] playerImageArr;

        BufferedImage[] dogImageArr;

        BufferedImage[][] skateBoardImageArr;

        BufferedImage[] hearts;

        BufferedImage[] lights;

        BufferedImage defend;

        BufferedImage infinite;

        BufferedImage smallCoin;

        BufferedImage warnBoard;

        BufferedImage downBoard;

        BufferedImage flag;

        BufferedImage redFlag;

        BufferedImage hole;

        BufferedImage wastedSkateBoard;

        BufferedImage wastedSingleBoard;

        BufferedImage bin;

        BufferedImage stub;

        BufferedImage[] snowMan;

        BufferedImage[] woods;

        BufferedImage[] shrubs;

        BufferedImage[] smallTrees;

        BufferedImage deadBoard;

        BufferedImage[] bushes;

        BufferedImage[] lakes;

        BufferedImage[] bigRocks;

        BufferedImage bigHouse;

        BufferedImage[] smallRocks;

        BufferedImage[] bigTrees;


        public ImgFactory(){
            try {
                bkgImg = ImageIO.read(new FileInputStream("C:\\Users\\chenj\\IdeaProjects\\mygw\\src\\test\\java\\com\\example\\mygw\\bg.png"));
                playerImage = ImageIO.read(new FileInputStream("C:\\Users\\chenj\\IdeaProjects\\mygw\\src\\test\\java\\com\\example\\mygw\\player.png"));
                objectsImage = ImageIO.read(new FileInputStream("C:\\Users\\chenj\\IdeaProjects\\mygw\\src\\test\\java\\com\\example\\mygw\\objects.png"));
                initPlayerImage();
                initObjectsImage();
                initDogImage();

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void initPlayerImage(){
            playerImageArr = new BufferedImage[9][10];
            int sx = 0,sy = 251;
            for(int i = 0; i< 9 ;i++){
                for(int j = 0;j < 10; j++){
                    playerImageArr[i][j] = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D sk_g = (Graphics2D) (playerImageArr[i][j].getGraphics());
                    sk_g.drawImage(playerImage.getSubimage(sx + j * 64, sy + i * 64, 64, 64), 0, 0, 64, 64, null);
                    sk_g.dispose();
                }
            }
            sx = sy =0;
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

        public void initDogImage(){}

        public void initObjectsImage(){
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
        }

        public void initHearts(){
            hearts = new BufferedImage[2];
            hearts[0] = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            hearts[1] = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,hearts[0],0,0,24,24);
            copy(objectsImage,hearts[1],0,24,24,24);
        }

        public void initLights(){
            lights = new BufferedImage[2];
            lights[0] = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            lights[1] = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,lights[0],24,0,24,24);
            copy(objectsImage,lights[1],24,24,24,24);
        }

        public void initDefend(){
            defend=new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,defend,48,0,24,24);
        }

        public void initInfinite(){
            infinite = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,infinite,48,24,24,24);
        }

        public void initSmallCoin(){
            smallCoin = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,smallCoin,72,0,24,24);
        }

        public void initWarnBoard(){
            warnBoard = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,warnBoard,6*64,0,64,64);
        }

        public void initDownBoard(){
            downBoard = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,downBoard,7*64,0,64,64);
        }

        public void initFlag(){
            flag = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,flag,8*64,0,64,64);
        }

        public void initRedFlag(){
            redFlag = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,redFlag,9*64,0,64,64);
        }

        public void initHole(){
            hole = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,hole,10*64,0,64,64);
        }

        public void initWastedSkateBoard(){
            wastedSkateBoard = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,wastedSkateBoard,11*64,0,64,64);
        }

        public void initWastedSingleBoard(){
            wastedSingleBoard = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,wastedSingleBoard,12*64,0,64,64);
        }

        public void initBin(){
            bin = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,bin,13*64,0,64,64);
        }

        public void initStub(){
            stub = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,stub,14*64,0,64,64);
        }

        public void initSnowMan(){
            snowMan = new BufferedImage[2];
            snowMan[0] = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            snowMan[1] = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,snowMan[0],0,64,64,64);
            copy(objectsImage,snowMan[1],64,64,64,64);
        }

        public void initWoods(){
            woods = new BufferedImage[2];
            woods[0] = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            woods[1] = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,woods[0],2*64,64,64,64);
            copy(objectsImage,woods[1],3*64,64,64,64);
        }

        public void initShrubs(){
            shrubs = new BufferedImage[2];
            shrubs[0] = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            shrubs[1] = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,shrubs[0],4*64,64,64,64);
            copy(objectsImage,shrubs[1],5*64,64,64,64);
        }

        public void initSmallTrees(){
            smallTrees = new BufferedImage[5];
            for(int i  = 0 ;i<smallTrees.length;i++){
                smallTrees[i] = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
                copy(objectsImage,smallTrees[i],(6+i)*64,64,64,64);
            }
        }

        public void initDeadBoard(){
            deadBoard = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,deadBoard,11*64,64,64,64);
        }

        public void  initBushes(){
            bushes = new BufferedImage[3];
            for(int i  = 0 ;i<bushes.length;i++){
                bushes[i] = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
                copy(objectsImage,bushes[i],(12+i)*64,64,64,64);
            }
        }

        public void initLakes(){
            lakes = new BufferedImage[3];
            for(int i  = 0 ;i<lakes.length;i++){
                lakes[i] = new BufferedImage(128,128,BufferedImage.TYPE_INT_ARGB);
                copy(objectsImage,lakes[i],(0+i)*128,128,128,128);
            }
        }

        public void initBigRocks(){
            bigRocks = new BufferedImage[2];
            bigRocks[0] = new BufferedImage(64*3,64*2,BufferedImage.TYPE_INT_ARGB);
            bigRocks[1] = new BufferedImage(64*3,64*2,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,bigRocks[0],3*128,128,192,128);
            copy(objectsImage,bigRocks[0],3*128 + 192*2,128,192,128);
        }

        public void initBigHouse(){
            bigHouse = new BufferedImage(64*3,64*2,BufferedImage.TYPE_INT_ARGB);
            copy(objectsImage,bigHouse,3*128+192,128,192,128);
        }

        public void initSmallRocks(){
            smallRocks = new BufferedImage[3];
            for (int i = 0;i<smallRocks.length;i++){
                smallRocks[i] = new BufferedImage(128,128,BufferedImage.TYPE_INT_ARGB);
                copy(objectsImage,smallRocks[i],(3+i)*128,256,128,128);
            }
        }

        public void initBigTrees(){
            bigTrees = new BufferedImage[5];
            for (int i = 0;i<bigTrees.length;i++){
                bigTrees[i] = new BufferedImage(64,128,BufferedImage.TYPE_INT_ARGB);
            }
            copy(objectsImage,bigTrees[0],12*64,256,64,128);
            copy(objectsImage,bigTrees[1],13*64,256,64,128);
            copy(objectsImage,bigTrees[2],14*64,256,64,128);
            copy(objectsImage,bigTrees[3],13*64,384,64,128);
            copy(objectsImage,bigTrees[4],14*64,384,64,128);
        }



        public static void copy(BufferedImage source,BufferedImage dest,int sx, int sy, int width, int height){
            Graphics2D g = (Graphics2D) dest.getGraphics();
            g.drawImage(source.getSubimage(sx,sy,width,height),0,0,width,height,null);
            g.dispose();
        }

    }



    public static void main(String[] args) {
        Surf surf = new Surf();
        surf.run();
    }
}
