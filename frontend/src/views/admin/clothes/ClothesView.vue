<template>
  <a-modal v-model="show" title="独家消息" @cancel="onClose" :width="800" class="exclusive-message-modal">
    <template slot="footer">
      <a-button key="back" @click="onClose" type="primary" class="close-btn">
        确定
      </a-button>
    </template>
    <div class="exclusive-content-wrapper" v-if="orderData !== null">
      <div class="message-header">
        <div class="header-badge">独家消息</div>
        <h2 class="message-title">{{ orderData.title }}</h2>
      </div>

      <div class="message-meta">
        <div class="uploader-info">
          <a-icon type="user" class="icon-user" />
          <span class="uploader-name">{{ orderData.uploader }}</span>
        </div>
        <div class="upload-time">
          <a-icon type="clock-circle" class="icon-clock" />
          <span class="time-text">{{ formatDate(orderData.createTime || new Date()) }}</span>
        </div>
      </div>

      <div class="message-content">
        <p>{{ orderData.content }}</p>
      </div>

      <div class="message-gallery" v-if="fileList.length > 0">
        <h3 class="gallery-title">专属图片</h3>
        <div class="image-preview-container">
          <div
            v-for="(file, index) in fileList"
            :key="index"
            class="image-item"
            @click="handlePreview(file)"
          >
            <img :src="file.url" :alt="'图片' + (index+1)" class="preview-image" />
          </div>
        </div>
      </div>

      <div class="exclusive-footer">
        <a-icon type="star" class="icon-star" />
        <span>粉丝专属内容 - 仅限粉丝查看</span>
      </div>
    </div>

    <a-modal
      :visible="previewVisible"
      :footer="null"
      @cancel="handleCancel"
      class="preview-modal"
    >
      <img alt="预览图" style="width: 100%" :src="previewImage" />
    </a-modal>
  </a-modal>
</template>

<script>import moment from 'moment'
moment.locale('zh-cn')

function getBase64 (file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}

export default {
  name: 'ClothesView',
  props: {
    orderShow: {
      type: Boolean,
      default: false
    },
    orderData: {
      type: Object
    }
  },
  computed: {
    show: {
      get: function () {
        return this.orderShow
      },
      set: function () {
      }
    }
  },
  data () {
    return {
      loading: false,
      fileList: [],
      previewVisible: false,
      previewImage: '',
      repairInfo: null,
      reserveInfo: null
    }
  },
  watch: {
    orderShow: function (value) {
      if (value) {
        if (this.orderData.images !== null && this.orderData.images !== '') {
          this.imagesInit(this.orderData.images)
        } else {
          this.fileList = []
        }
      }
    }
  },
  methods: {
    formatDate(date) {
      return moment(date).format('YYYY-MM-DD HH:mm')
    },
    imagesInit (images) {
      if (images !== null && images !== '') {
        let imageList = []
        images.split(',').forEach((image, index) => {
          imageList.push({
            uid: index,
            name: image,
            status: 'done',
            url: 'http://127.0.0.1:9527/imagesWeb/' + image
          })
        })
        this.fileList = imageList
      }
    },
    handleCancel () {
      this.previewVisible = false
    },
    async handlePreview (file) {
      if (!file.url && !file.preview) {
        file.preview = await getBase64(file.originFileObj)
      }
      this.previewImage = file.url || file.preview
      this.previewVisible = true
    },
    picHandleChange ({ fileList }) {
      this.fileList = fileList
    },
    onClose () {
      this.$emit('close')
    }
  }
}
</script>

<style scoped>.exclusive-message-modal .ant-modal-content {
  border-radius: 12px;
  overflow: hidden;
}

.message-header {
  text-align: center;
  padding: 20px 0 15px;
  /*background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);*/
  color: black;
  position: relative;
}

.header-badge {
  position: absolute;
  top: 15px;
  right: 24px;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.message-title {
  margin: 0;
  font-size: 22px;
  font-weight: bold;
  color: black;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  padding: 15px 24px;
  background-color: #f9f9f9;
  border-bottom: 1px solid #eee;
}

.uploader-info, .upload-time {
  display: flex;
  align-items: center;
  color: #666;
  font-size: 14px;
}

.icon-user, .icon-clock, .icon-star {
  margin-right: 8px;
  color: #ff6b35;
}

.message-content {
  padding: 24px;
  line-height: 1.8;
  font-size: 15px;
  color: #333;
  min-height: 120px;
}

.message-content p {
  margin: 0;
}

.message-gallery {
  padding: 0 24px 24px;
}

.gallery-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

.image-preview-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.image-item {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.image-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.preview-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
}

.exclusive-footer {
  padding: 15px 24px;
  background-color: #fff9db;
  border-top: 1px solid #ffe58f;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d48806;
  font-size: 14px;
}

.close-btn {
  border: none;
  border-radius: 20px;
  padding: 6px 24px;
}

.close-btn:hover {
  opacity: 0.9;
}

.preview-modal .ant-modal-body {
  padding: 0;
}
</style>
