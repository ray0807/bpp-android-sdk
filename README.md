## BPG是一种新型的图片格式。其设计初衷在于当图片质量或文件size成为瓶颈时，取代JPEG。
### BPG的讲解具体可以参考：https://andremouche.github.io/graphics/graphics-BPG.html

#### 此sdk主要是Android适配主要的几个图片库，如：freco，imagedownloader等等（我也不记得了，很久之前开发的）。

### 工作流程

#### 手机端（ios,android）压缩图片成bpg格式-->手机上传bpg-->保存oss-->手机端下载bpg-->解压展示（使用到Android图片库）
#### 添加元素：添加了图片加密解密，均在so中完成，主要因为鉴权。
