# GIT详解

#### 1.首先安装git

在gitbash上先配置基础连接，

git config --global user.name "ToolsPeople"

git config --global user.email 1813211082@qq.com

### 2.了解它的工作原理

![image-20220123151906729](C:/Users/Jason/AppData/Roaming/Typora/typora-user-images/image-20220123151906729.png)

### 3.创建本地仓库

创建本地仓库又两种方式

一种是创建全新的仓库

git init

另一种是克隆远程仓库

git clone git@github.com:ToolsPeople/-.git

后面的是你github上的地址

![image-20220123153314191](C:/Users/Jason/AppData/Roaming/Typora/typora-user-images/image-20220123153314191.png)

 经过这一步后，你就会发现你在原来创建的本地文件夹中多了一个文件

![image-20220123153610180](C:/Users/Jason/AppData/Roaming/Typora/typora-user-images/image-20220123153610180.png)

![image-20220123153624332](C:/Users/Jason/AppData/Roaming/Typora/typora-user-images/image-20220123153624332.png)

这个文件就是你github网上的仓库，同步克隆进来了

