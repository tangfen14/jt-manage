package com.jt.manage.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jt.common.vo.PicUploadResult;

@Service
public class FileServiceImpl implements FileService {
	
	//要求参数应该动态获取,而不是写死的
	//定义文件存储的根目录
	@Value("${image.localPath}") //通过注解的方式和配置文件, 为属性动态赋值
	private String localPath;  //= "E:/jt-upload/";
	
	//定义虚拟路径的根目录
	@Value("${image.urlPath}")
	private String urlPath;    //="http://image.jt.com/";
	
	/**
	 * 需要考虑的问题:
	 * 	1.是否为正确的图片??? 判断后缀是否是:jpg|png|gif  即可
	 * 	2.是否为恶意程序(比如将木马伪装成jpg)???   
	 *	3.不能将图片保存到同一个文件夹下
	 *  4.图片的重名问题
	 *   解决策略:
	 *  1.正则表达式实现图片的判断.
	 *  2.使用BufferedImage工具转化图片,如果是一张图片,那么一定可以获取它的 height/weight,如果不是个图片
	 *  		一转换就报异常了,即使不报异常那得到的宽高一定是0/0
	 *  3.使用分文件夹存储,比如按 yyyy/MM/dd 年月日分三级(并不是唯一的方法)
	 *  4.UUID+三位随机数区分图片
	 */
	@Override
	public PicUploadResult upload(MultipartFile uploadFile) {
		PicUploadResult result = new PicUploadResult();
		
		//1.获取图片的名称   abc.jpg
		String fileName = uploadFile.getOriginalFilename();
		fileName = fileName.toLowerCase();//转换为小写
		
		//2.判断是否为图片的类型
		/*复习正则:   1 一般以^开始,以$结束
				  2  以.表示任意一个字符(除了回车和换行符以外),多个字符的换用  .*
				  3 判断是否属于其中的一项用分组的形式   (jpg|png|gif)
				 */
		if(!fileName.matches("^.*(jpg|png|gif)$")){
			
			result.setError(1); //表示不是图片
            //说明:如果传递的图片不是指定的格式return
            return result;
		}
		
		//3.判断是否为恶意程序
		try {
			//用过ImageIO工具创建一个bufferedImage
			BufferedImage bufferedImage = 
					ImageIO.read(uploadFile.getInputStream());
			
			int height = bufferedImage.getHeight();
			int width = bufferedImage.getWidth();
			if(height == 0 || width == 0){
				
				result.setError(1);
				return result;
			}
			
			//4.将图片分文件存储 yyyy/MM/dd
			String DatePath = 
					new SimpleDateFormat("yyyy/MM/dd").format(new Date());
			
			//判断是否有该文件夹  E:/jt-upload/2018/11/11
			String picDir = localPath + DatePath;
			File picFile = new File(picDir);
			
			//是否有这个文件夹,没有的话新建一个
			if(!picFile.exists()){
				
				picFile.mkdirs();
			}
			
			//防止文件重名   
			//UUID.randomUUID()生成的是  1111-djskada-jdlsak-jdlsajd 格式的,我们将其中的-去掉
			String uuid = UUID.randomUUID().toString().replace("-", "");
				//再生成3位随机数
			int randomNum = new Random().nextInt(1000);
			//获取文件类型(后缀)  如.jpg
			String fileType = fileName.substring(fileName.lastIndexOf("."));
			
			//拼接新的文件名称
			String fileNowName = uuid + randomNum + fileType;
			
			//核心:实现文件上传            e:jt-upload/yyyy/MM/dd/1231231231231231231.jpg
				//首先准备一个文件的全路径
			String realFilePath = picDir + "/" +fileNowName;
			uploadFile.transferTo(new File(realFilePath));

			
			//将真实数据回显,+""拼串
			result.setHeight(height+"");
			result.setWidth(width+"");
			
			/**
			 * 实现虚拟路径的拼接
			 * 真实路径E:/jt-upload/2018/07/23/e4d5c2667a174477b2ab59158670bbbe816.jpg
			 * 虚拟路径image.jt.com
			 */
			String realUrl = urlPath + DatePath + "/" + fileNowName;
			result.setUrl(realUrl); 
			
		} catch (Exception e) {
			e.printStackTrace();
			result.setError(1); //文件长传有误
		}
		return result;
	}

}
