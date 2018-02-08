package com.palmg.security.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.palmg.security.properties.exception.FileWriteException;

/**
 * 独立允许操作入口, 通过Jvm命令可以指定要操作的内容。<br>
 * --option-[params]表示操作。[params]为具体参数。如下：<br>
 * key：生成加密密钥。注意当前密钥的profile。 data：根据properties文件生成加密文件。注意对应的配置参数。
 * --params-[key]-[value] 表示参数。 [key]为具体参数名称。 [value]为参数数值。
 * 例如：--params-scheme-AES 如下：<br>
 * scheme：加密方案，目前仅仅提供AES方案。<br>
 * keyFileName：生成的加密密钥文件的名称，默认为PalmgEnvCert.key。<br>
 * loadPath：文件加载路径，可以用,号分割多个加载路径，采用spring ResourcePatternResolver的加载规范。<br>
 * writePath：生成文件的路径，包括密钥文件和加密文件。<br>
 * profile：环境指定参数，会影响密钥文件的路径名称。默认为default。<br>
 * profileFlag：加密替换标记。默认为${profile}。<br>
 * propertiesFileName：加密文件的名称。默认为properties-${profile}.data。${profile}会使用密钥文件的profile替换。<br>
 * seed：密钥生成的随机种子。取前16位。<br>
 * 
 * @param args
 * @throws FileWriteException
 * @throws IOException
 */
public class App {
	static Logger LOG = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		try {
			new App().run(args);
		} catch (FileWriteException e) {
			LOG.error("Run App error!", e);
		}
	}

	private void run(String[] args) throws FileWriteException {
		String option = "--option-data";
		for (String arg : args) {
			if (arg.startsWith("--option-")) {
				option = arg;
			} else if (arg.startsWith("--params-")) {
				String[] k_v = arg.replace("--params-", "").split("-");
				System.setProperty("palmg-security-" + k_v[0], k_v[1]);
			}
		}
		switch (option) {
		case "--option-key":
			PropertyCrypto.getNew().generateKeyFile();
			break;
		case "--option-data":
		default:
			PropertyCrypto.getNew().generateSecretFile();
			break;
		}
	}
}
