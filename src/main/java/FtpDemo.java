import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.util.Iterator;

import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpDirEntry;
import sun.net.ftp.FtpProtocolException;

/** * * @author 皮锋 java自带的API对FTP的操作 * */
public class FtpDemo {

    /**
     * * 服务器连接 * * @param ip * 服务器IP * @param port * 服务器端口 * @param user * 用户名 * @param
     * password * 密码 * @param path * 服务器路径
     */
    public static FtpClient connectServer(String ip, int port, String user, String password) {
        FtpClient ftpClient = null;
        try {
            ftpClient = FtpClient.create();
                SocketAddress addr = new InetSocketAddress(ip, port);
                ftpClient.connect(addr);
                ftpClient.login(user, password.toCharArray());
                ftpClient.enablePassiveMode(true);
        } catch (IOException | FtpProtocolException ex) {
            ex.printStackTrace();
        }
        return ftpClient;
    }



//    public static void main(String agrs[]) throws IOException, FtpProtocolException {
//        FtpClient ftpClient = connectServer("47.103.80.115", 21, "Maohd", "Mhd!@#test");
//        downloadFile(ftpClient,"/","D:\\data\\");
//        ftpClient.close();
//    }

    public static void main(String[] args) throws IOException, FtpProtocolException {
        FtpClient ftpClient = connectServer("47.103.80.115", 21, "Maohd", "Mhd!@#test");
        File file = new File("D:\\CEB512Message-20210303105804672575570.xml");
        uploadFile(ftpClient,"/tosend/",file);
        ftpClient.close();
    }

    //下载远程目录下的所有文件，递归子文件夹
    public static void downloadFile(FtpClient ftpClient,String remotePath,String localPath) throws IOException, FtpProtocolException {
        ftpClient.changeDirectory(remotePath);
        Iterator<FtpDirEntry> ftpDirEntryIterator = ftpClient.listFiles(remotePath);
        while (ftpDirEntryIterator.hasNext()) {
            FtpDirEntry next = ftpDirEntryIterator.next();
            //如果是文件
            if (FtpDirEntry.Type.FILE.equals(next.getType())) {
                InputStream fileStream = ftpClient.getFileStream(next.getName());
                FileOutputStream os = new FileOutputStream(localPath + next.getName());
                copyStream(os,fileStream);
            }
            //如果是文件夹
            if (FtpDirEntry.Type.DIR.equals(next.getType())) {
                File file = new File(localPath + next.getName());
                file.mkdir();
                downloadFile(ftpClient,remotePath+next.getName()+"/",localPath+next.getName()+"/");
            }
        }
    }

    /**
     * 流拷贝
     * @param os
     * @param is
     * @throws IOException
     */
    public static void copyStream(OutputStream os,InputStream is) throws IOException {
        try {
            byte[] bytes = new byte[1024];
            int c;
            while ((c = is.read(bytes)) != -1) {
                os.write(bytes, 0, c);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 上传文件（文件夹）到ftp服务器
     * @param ftpClient
     * @param remotePath
     * @param file
     * @throws IOException
     * @throws FtpProtocolException
     */
    public static void uploadFile(FtpClient ftpClient,String remotePath,File file) throws IOException, FtpProtocolException {
        ftpClient.changeDirectory(remotePath);
        if (file.isFile()) {
            FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream outputStream = ftpClient.putFileStream(remotePath+"/"+file.getName());
            copyStream(outputStream,fileInputStream);
        }
        if (file.isDirectory()) {
            String subRemotePath = remotePath+file.getName();
            ftpClient.makeDirectory(subRemotePath);
            for (File subFile : file.listFiles()) {
                uploadFile(ftpClient,subRemotePath,subFile);
            }
        }
    }
}