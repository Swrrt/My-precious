package topology;
import TranslateFramework.*;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.*;
import backtype.storm.tuple.Fields;

import java.io.*;
import java.util.*;

/**
 * Created by swrrt on 21/9/2015.
 *
 * Framework for CV
 * All video and image will be taged like "filename_pack_frame_patch_scale_spatch"
 *
 */
public class DEMOTranslate {
    public static void main(String args[])throws Exception{

        File infile = new File(args[0]);
        Map <String,String> bolt_table = new HashMap<>();
        Map <String,Fields> bolt_fields = new HashMap<>();
        Scanner in = new Scanner(infile);
        int line = 0;
        TopologyBuilder builder = new TopologyBuilder();
        Config conf = new Config();
        while(in.hasNext()) {
            String x = in.nextLine();
            if (x.substring(0, 5).contains("IMAGE")) { /*   Image spout :  IMAGE name="directory"   */
                String name, filename;
                int i = 6;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(6, i);
                i += 2;
                int j = i;
                while (x.charAt(i) != '"') {
                    i++;
                }
                filename = x.substring(j, i);
                bolt_table.put(name, "IMAGE");
                builder.setSpout(name, new TranslateFramework.ImageReadSpout(filename), 1); /* Now spout is limited to no parallelism */
            } else if (x.substring(0, 5).contains("VIDEO")) {    /* Video spout,  need to declare number of packed frame :  VIDEO x limit name="filename"   output a pack of frames*/
                String name, filename;
                int i = 6, y, j, limit;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(6, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                limit = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = (x.substring(j, i));
                i += 2;
                j = i;
                while (x.charAt(i) != '"') {
                    i++;
                }
                filename = (x.substring(j, i));
                //  System.out.println("????|"+name+"|");
                bolt_table.put(name, "VIDEO");
                builder.setSpout(name, new TranslateFramework.VideoFrameSpout(filename, y, limit), 1);

            } else if (x.substring(0, 6).contains("DEVIDE")) { /* Devide image into r*r patchs, x is parallelism: DEVIDE x r name=input1(,input2...)*/
                String name;
                int i = 7, y, j;
                double r;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(7, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                r = Double.parseDouble(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);

                BoltDeclarer xx = builder.setBolt(name, new TranslateFramework.DevideImageBolt(r), y);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }


                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
                //conf.put("DEVIDE_"+name+"_RATIO",r);
            } else if (x.substring(0, 5).contains("LK_OF")) {  /* LK Optical Flow Field : LK_OF x side sigma thres name=input1(,input2...) */
                String name;
                System.out.println("????|" + x + "|");
                int i = 6, side, j, y;
                double sigma, thres;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(6, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                side = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                sigma = Double.parseDouble(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                thres = Double.parseDouble(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                //System.out.println("??name|"+x.substring(j,i)+"|");
                BoltDeclarer xx = builder.setBolt(name, new TranslateFramework.OpticalFlowBolt(side, sigma, thres), y),xxx = builder.setBolt(name+"_latent", new OpticalFlowBolt_latent(),1);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    // System.out.println("??|"+x.substring(j,i)+"|");
                    xxx.fieldsGrouping(x.substring(j, i), new Fields("Filename", "Pack", "Patch", "Scale", "sPatch"));
                    i++;
                    j = i;
                } while (true);
                xx.shuffleGrouping(name+"_latent");
            } else if (x.substring(0, 9).contains("DENSETRAJ")) { /* Dense Trajectory : DENSETRAJ x frame median_side traj_side name=input1(,input2...) */
                String name;
                int frame, y, j = 10, i = 10, median, traj;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                frame = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                median = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                traj = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new TranslateFramework.DenseTrajectBolt(frame, median, traj), y);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.fieldsGrouping(x.substring(j, i), new Fields("Filename", "Pack", "Patch", "Scale", "sPatch"));
                    i++;
                    j = i;
                } while (true);
            } else if (x.substring(0, 3).contains("HOG")) {     /* HOG feature: HOG x side bin is_single name=input1(,input2...) */
                String name;
                int side, y, j = 4, i = 4, bin;
                boolean is_single;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                side = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                bin = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                is_single = Boolean.parseBoolean(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new TranslateFramework.HOGBolt(side, bin, is_single), y);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
            } else if (x.substring(0, 3).contains("HOF")) { /* HOF feature: HOF x side bin is_single name=input1(,input2...) */
                String name;
                int side, y, j = 4, i = 4, bin;
                boolean is_single;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                side = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                bin = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                is_single = Boolean.parseBoolean(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new TranslateFramework.HOFBolt(side, bin, is_single), y);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
            } else if (x.substring(0, 3).contains("MBH")) { /* MBH feature: MBH x side bin is_single name=input1(,input2...) */
                String name;
                int side, y, j = 4, i = 4, bin;
                boolean is_single;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                side = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                bin = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                is_single = Boolean.parseBoolean(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new TranslateFramework.MBHBolt(side, bin, is_single), y);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
            } else if (x.substring(0, 6).contains("FMERGE")) { /* Merge feature with same pack,patch,scale,spatch: FMERGE x npack name=input1(,input2...) */
                String name;
                int side, y, j = 7, i = 7, bin;
                boolean is_single;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                side = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name + "_Latent", new TranslateFramework.FeatureMergeBolt_Latent(), 1);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
                builder.setBolt(name, new TranslateFramework.FeatureMergeBolt(side), y).fieldsGrouping(name + "_Latent", new Fields("Filename", "Pack", "Patch", "Scale", "sPatch"));
            } else if (x.substring(0, 7).contains("SPMERGE")) { /* Merge sPatch's feature into list of feature: SPMERGE x npack name=input1(,input2...) */
                String name;
                int side, y, j = 8, i = 8, bin;
                boolean is_single;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                side = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name + "_Latent", new TranslateFramework.SPatchMergeBolt_Latent(), 1);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
                builder.setBolt(name, new TranslateFramework.SPatchMergeBolt(side), y).fieldsGrouping(name + "_Latent", new Fields("Filename", "Pack", "Frame", "Patch", "Scale"));
            } else if (x.substring(0, 5).contains("BOW_T")) { /* Train a BOW: BOW_T filename limit n iter name=input1(,input2...) */
                String name, filename;
                int j = 6, i = 6, limit, n, iter;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                filename = x.substring(j, i);
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                limit = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                n = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                iter = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new TranslateFramework.BOW_trainBolt(filename, limit, n, iter), 1);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
            } else if (x.substring(0, 3).contains("BOW")) {  /* BOW x filename name=input1(,input2...) */
                String name, filename;
                int j = 4, i = 4, y;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                filename = x.substring(j, i);
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new TranslateFramework.BOWBolt(filename), y);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
            } else if (x.substring(0, 5).contains("SVM_T")) {   /*  train SVM: SVM_T filename limit name=input1(,input2...) */
                String name, filename;
                int j = 6, i = 6, y;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                filename = x.substring(j, i);
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new TranslateFramework.SVM_trainBolt(filename, y), 1);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
            } else if (x.substring(0, 3).contains("SVM")) { /* Using SVM predict: SVM x filename name=input1(,input2...) */
                String name, filename;
                int j = 4, i = 4, y;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                filename = x.substring(j, i);
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new TranslateFramework.SVMBolt(filename), y);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
            } else if (x.substring(0, 4).contains("FOUT")) {  /* Output feature or features to files by filename: FOUT x path name=input1(,input2...) */
                String name, path;
                int j = 5, i = 5, y;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                path = x.substring(j, i);
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new TranslateFramework.OutputToFileBolt(path), y);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
            } else if (x.substring(0, 9).contains("TRAJSHAPE")) { /* Dense Trajectory shape feature : TRAJSHAPE x name=input1(,input2...) */
                String name, path;
                int j = 10, i = 10, y;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new TrajShapeFeatureBolt(), y);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
            } else if (x.substring(0, 7).contains("DRAWSVM")) {  /* Draw a a rectangle with SVM information on the images: DRAWSVM name=input1(,input2...) */
                String name, path;
                int j = 8, i = 8, y;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new DrawSVMBolt(), 1);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
            } else if(x.substring(0,11).contains("SCALEDIVIDE")){ /* Divide one image to different scales: SCALEDIVIDE x ratio n name=input1(,input2...) */
                String name, path;
                int j = 12, i = 12, y,n;
                double r;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                r = Double.parseDouble(x.substring(j,i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                n = Integer.parseInt(x.substring(j,i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new ScaleDevideBolt(r,n), y);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.shuffleGrouping(x.substring(j, i));
                    i++;
                    j = i;
                } while (true);
            }else if(x.substring(0,10).contains("SCALEMERGE")){ /* Merge features from different scale: SCALEMERGE x n name=input1(,input2...)*/
                String name, path;
                int j = 11, i = 11, y,r,n;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                y = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                n = Integer.parseInt(x.substring(j,i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new ScaleMergeBolt(n), y);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.fieldsGrouping(x.substring(j, i),new Fields("Filename","Pack","Frame","Patch","sPatch"));
                    i++;
                    j = i;
                } while (true);
            }else if(x.substring(0,6).contains("FILTER")){/* Filter with specific values in specific fields: FILTER x field1:value1(,field2:value2...) name=input1(,input2...)*/
                List<String> field,value;
                String name;
                int y;
                field = new ArrayList<>();
                value = new ArrayList<>();
                int i=7,j=7;
                while(x.charAt(i) != ' '){
                    i++;
                }
                y = Integer.parseInt(x.substring(j,i));
                i++;
                j=i;
                do{
                    while(x.charAt(i)!=':')i++;
                    field.add(x.substring(j,i));
                    i++;
                    j=i;
                    while(x.charAt(i)!=','&&x.charAt(i)!=' ')i++;
                    value.add(x.substring(j,i));
                    i++;
                    j=i;
                }while(x.charAt(i-1)==',');
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j, i);
                BoltDeclarer xx = builder.setBolt(name, new FilterBolt(field,value), y);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.fieldsGrouping(x.substring(j, i),new Fields("Filename","Pack","Frame","Patch","sPatch"));
                    i++;
                    j = i;
                } while (true);
            }else if(x.substring(0,10).contains("REDISSPOUT")){ /* Spout reading frames from redis queue: REDISSPOUT npack name="host:port:queue" */
                String name, host,queue;
                int i = 11, y, j=11, npack,port;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                npack = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = (x.substring(j, i));
                i += 2;
                j = i;
                while (x.charAt(i) != ':') {
                    i++;
                }
                host = (x.substring(j, i));
                i ++;
                j = i;
                while (x.charAt(i) != ':') {
                    i++;
                }
                port = Integer.parseInt(x.substring(j, i));
                i ++;
                j = i;
                while (x.charAt(i) != '"') {
                    i++;
                }
                queue = (x.substring(j, i));
                //  System.out.println("????|"+name+"|");
                //bolt_table.put(name, "VIDEO");
                builder.setSpout(name, new TranslateFramework.RedisSpout(host,port,queue,npack), 1);
            }else if(x.substring(0,8).contains("REDISOUT")){ /* Output frames to redis queue: REDISOUT npack host:port:queue name=input1(,input2...) */
                String name, host,queue;
                int j = 9, i = 9, npack,port;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                npack = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ':') {
                    i++;
                }
                host = x.substring(j, i);
                i++;
                j = i;
                while (x.charAt(i) != ':') {
                    i++;
                }
                port = Integer.parseInt(x.substring(j, i));
                i++;
                j = i;
                while (x.charAt(i) != ' ') {
                    i++;
                }
                queue = x.substring(j,i);
                i++;
                j = i;
                while (x.charAt(i) != '=') {
                    i++;
                }
                name = x.substring(j,i);
                BoltDeclarer xx = builder.setBolt(name, new RedisOutputBolt(host,port,queue,npack), 1);
                i++;
                j = i;
                do {
                    if (i >= x.length()) break;
                    while (i < x.length() && x.charAt(i) != ',') {
                        i++;
                    }
                    //System.out.println(x.substring(j,i));
                    xx.fieldsGrouping(x.substring(j, i),new Fields("Filename","Pack","Frame","Patch","sPatch"));
                    i++;
                    j = i;
                } while (true);
            }else if(x.substring(0,6).contains("VALVE ")){

                int numberin,numberout;

                String name;

                int i=6,j=6;

                while(x.charAt(i)!=' ')i++;

                numberin = Integer.parseInt(x.substring(j,i));

                i++;

                j=i;

                while(x.charAt(i)!=' ')i++;

                numberout = Integer.parseInt(x.substring(j,i));

                i++;

                j=i;

                while(x.charAt(i)!='=')i++;

                name = x.substring(j,i);

                BoltDeclarer xx = builder.setBolt(name, new ValveBolt(numberin,numberout), 1);

                i++;

                j = i;

                do {

                    if (i >= x.length()) break;

                    while (i < x.length() && x.charAt(i) != ',') {

                        i++;

                    }

                    //System.out.println(x.substring(j,i));

                    xx.fieldsGrouping(x.substring(j, i),new Fields("Filename","Pack","Frame","Patch","sPatch"));

                    i++;

                    j = i;

                } while (true);

            }else if(x.substring(0,6).contains("VALVES")){

                int number,timeout;

                String name,path,vname;

                int i=7,j=7;

                while(x.charAt(i)!=' ')i++;

                timeout = Integer.parseInt(x.substring(j,i));

                i++;

                j=i;

                while(x.charAt(i)!=' ')i++;

                number = Integer.parseInt(x.substring(j,i));

                i++;

                j=i;

                while(x.charAt(i)!=' ')i++;

                vname = x.substring(j,i);

                i++;

                j=i;

                while(x.charAt(i)!='=')i++;

                name = x.substring(j,i);

                SpoutDeclarer xx = builder.setSpout(name+"_virtual", new ValveVirtualSpout(timeout,number));

                i+=2;

                j = i;

                while(x.charAt(i)!='"')i++;

                path= x.substring(j,i);

                BoltDeclarer x1 = builder.setBolt(name,new ValveSpoutBolt(path),1);

                x1.shuffleGrouping(name+"_virtual");

                x1.shuffleGrouping(vname);

            }
        }
        conf.put(Config.TOPOLOGY_DEBUG, false);
        conf.setStatsSampleRate(1.0);
        conf.setMaxSpoutPending(2);
        conf.setMessageTimeoutSecs(120000);
        if (args!=null && args.length > 1){
            conf.setNumWorkers(40);
            StormSubmitter.submitTopology(args[1], conf, builder.createTopology());
        }else{
            conf.setMaxTaskParallelism(3);
            LocalCluster cluster = new LocalCluster();
            
            cluster.submitTopology("imageread", conf, builder.createTopology());
            Thread.sleep(30000);
            //cluster.shutdown();
        }
    }
}
