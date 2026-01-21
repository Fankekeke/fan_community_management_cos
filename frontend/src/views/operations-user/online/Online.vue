
<template>
  <div class="online-container" style="width: 100%">
    <a-card :bordered="false" class="card-area">
      <div class="online-grid">
        <a-card
          v-for="item in onlineList"
          :key="item.id"
          class="online-item"
          hoverable
        >
          <div class="live-cover">
            <img
              :src="'http://127.0.0.1:9527/imagesWeb/' + item.coverImage"
              class="cover-image"
              @click="showLiveDetail(item)"
              alt="直播封面"
            />
            <div class="live-status" :class="getStatusClass(item)">
              {{ getStatusText(item) }}
            </div>
            <div class="vip-badge" v-if="item.isVipOnly === '1'">
              VIP
            </div>
          </div>

          <div class="live-info">
            <div class="live-title" @click="showLiveDetail(item)">
              {{ item.title }}
            </div>
            <div class="live-time">
              <a-icon type="clock-circle" class="time-icon" />
              <span>{{ formatTimeRange(item.startTime, item.endTime) }}</span>
            </div>
            <div class="live-actions">
              <a-button
                type="primary"
                @click="enterLive(item)"
                :disabled="!canEnterLive(item)"
                class="watch-btn"
              >
                {{ canEnterLive(item) ? '进入直播间' : '未开始/已结束' }}
              </a-button>
              <a-button @click="showLiveDetail(item)" class="detail-btn">
                查看详情
              </a-button>
            </div>
          </div>
        </a-card>
      </div>
    </a-card>

    <!-- 直播详情弹窗 -->
    <a-modal
      v-model="detailModalVisible"
      :title="currentLive ? currentLive.title : '直播详情'"
      @cancel="hideLiveDetail"
      :width="800"
      :footer="null"
    >
      <div v-if="currentLive" class="live-detail-content">
        <div class="detail-item">
          <div class="detail-label">直播标题:</div>
          <div class="detail-value">{{ currentLive.title }}</div>
        </div>

        <div class="detail-item">
          <div class="detail-label">直播时间:</div>
          <div class="detail-value">{{ formatTimeRange(currentLive.startTime, currentLive.endTime) }}</div>
        </div>

        <div class="detail-item">
          <div class="detail-label">VIP专享:</div>
          <div class="detail-value" :class="currentLive.isVipOnly === '1' ? 'vip-status' : 'normal-status'">
            {{ currentLive.isVipOnly === '1' ? '是' : '否' }}
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-label">直播状态:</div>
          <div class="detail-value" :class="getStatusClass(currentLive)">
            {{ getStatusText(currentLive) }}
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-label">封面图片:</div>
          <div class="cover-image-container">
            <img
              :src="'http://127.0.0.1:9527/imagesWeb/' + currentLive.coverImage"
              class="cover-image"
              alt="直播封面"
            />
          </div>
        </div>

        <div class="detail-actions">
          <a-button
            type="primary"
            size="large"
            @click="enterLive(currentLive)"
            :disabled="!canEnterLive(currentLive)"            style="width: 100%;"
          >
            {{ canEnterLive(currentLive) ? '进入直播间' : '未开始/已结束' }}
          </a-button>
        </div>
      </div>
    </a-modal>

    <!-- 直播间弹窗 -->
    <a-modal
      v-model="liveRoomModalVisible"
      :title="currentLiveRoom ? currentLiveRoom.title : '直播间'"
      @cancel="exitLiveRoom"
      :width="1000"
      :footer="null"
      :bodyStyle="{ padding: '0' }"
    >
      <div v-if="currentLiveRoom" class="live-room-content">
        <div class="video-container">
          <video
            ref="videoPlayer"
            class="video-js vjs-default-skin"
            controls
            preload="auto"
            width="100%"
            height="500"
          ></video>
        </div>

        <div class="live-room-info">
          <h3>{{ currentLiveRoom.title }}</h3>
          <div class="room-time">
            <a-icon type="clock-circle" />
            <span>{{ formatTimeRange(currentLiveRoom.startTime, currentLiveRoom.endTime) }}</span>
          </div>
          <div class="room-vip" v-if="currentLiveRoom.isVipOnly === '1'">
            <a-icon type="star" />
            <span>VIP专享</span>
          </div>
          <div class="room-status">
            <span :class="getStatusClass(currentLiveRoom)">{{ getStatusText(currentLiveRoom) }}</span>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>import RangeDate from '@/components/datetime/RangeDate'
import BulletinAdd from './OnlineAdd.vue'
import BulletinEdit from './OnlineEdit.vue'
import {mapState} from 'vuex'
import videojs from 'video.js';
import 'video.js/dist/video-js.css';
import moment from 'moment'
moment.locale('zh-cn')

export default {
  name: 'Online',
  components: {BulletinAdd, BulletinEdit, RangeDate},
  data () {
    return {
      onlineList: [],
      detailModalVisible: false,
      liveRoomModalVisible: false,
      currentLive: null,
      currentLiveRoom: null,
      player: null,
      memberInfo: null
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    })
  },
  mounted () {
    this.queryOnlineList()
    this.selectMemberByUserId()
  },
  methods: {
    selectMemberByUserId () {
      this.$get(`/cos/member-order-info/member/${this.currentUser.userId}`).then((r) => {
        this.memberInfo = r.data.member
      })
    },
    queryOnlineList () {
      this.$get('/cos/online-events/list').then(res => {
        this.onlineList = res.data.data
      })
    },
    // 获取直播状态文本
    getStatusText(item) {
      const now = moment()
      const start = moment(item.startTime)
      const end = moment(item.endTime)

      if (now.isBefore(start)) {
        return '未开始'
      } else if (now.isBetween(start, end)) {
        return '直播中'
      } else {
        return '已结束'
      }
    },
    // 获取直播状态样式类
    getStatusClass(item) {
      const now = moment()
      const start = moment(item.startTime)
      const end = moment(item.endTime)

      if (now.isBefore(start)) {
        return 'status-inactive'
      } else if (now.isBetween(start, end)) {
        return 'status-active'
      } else {
        return 'status-inactive'
      }
    },
    // 格式化时间范围
    formatTimeRange(startTime, endTime) {
      return `${moment(startTime).format('MM月DD日 HH:mm')} - ${moment(endTime).format('MM月DD日 HH:mm')}`
    },
    // 判断是否可以进入直播间
    canEnterLive(item) {
      const now = moment()
      const start = moment(item.startTime)
      const end = moment(item.endTime)

      // 检查时间范围
      const isWithinTime = now.isBetween(start, end)

      // 如果是VIP专享，检查用户是否为VIP
      if (item.isVipOnly === '1' && !this.memberInfo) {
        return false
      }

      return isWithinTime
    },
    // 进入直播间
    enterLive(item) {
      if (this.canEnterLive(item)) {
        // 检查是否为VIP专享
        if (item.isVipOnly === '1' && !this.memberInfo) {
          this.$message.warning('该直播为VIP专享，请成为会员后观看')
          return
        }

        this.currentLiveRoom = item
        this.liveRoomModalVisible = true

        // 延迟初始化视频播放器
        this.$nextTick(() => {
          this.initVideoPlayer()
        })
      } else {
        this.$message.info('直播未开始或已结束')
      }
    },
    // 初始化视频播放器
    initVideoPlayer() {
      if (this.player) {
        this.player.dispose() // 销毁之前的实例
      }

      this.player = videojs(
        this.$refs.videoPlayer,
        {
          autoplay: true,
          controls: true,
          responsive: true,
          fluid: true,
          sources: [{
            src: this.currentLiveRoom.liveUrl,
            type: 'application/x-mpegURL' // m3u8格式
          }]
        },
        () => {
          console.log('播放器初始化完成')
        }
      )
    },
    // 退出直播间
    exitLiveRoom() {
      this.liveRoomModalVisible = false
      if (this.player) {
        this.player.pause()
        this.player.dispose()
        this.player = null
      }
      this.currentLiveRoom = null
    },
    // 显示直播详情弹窗
    showLiveDetail(item) {
      this.currentLive = item
      this.detailModalVisible = true
    },
    // 隐藏直播详情弹窗
    hideLiveDetail() {
      this.detailModalVisible = false
      this.currentLive = null
    }
  },
  beforeDestroy() {
    // 页面销毁前清理播放器
    if (this.player) {
      this.player.dispose()
      this.player = null
    }
  },
  watch: {}
}
</script>

<style lang="less" scoped>@import "../../../../static/less/Common";

.online-container {
  padding: 20px;
}

.card-area {
  background: #fff;
}

.online-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  padding: 10px;
}

.online-item {
  margin-bottom: 0;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 16px rgba(0,0,0,0.1);
  }
}

.live-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;

  .cover-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    cursor: pointer;
    transition: transform 0.3s ease;

    &:hover {
      transform: scale(1.05);
    }
  }

  .live-status {
    position: absolute;
    top: 10px;
    left: 10px;
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: bold;
    color: white;

    &.status-active {
      background: #52c41a;
    }

    &.status-inactive {
      background: #bfbfbf;
    }
  }

  .vip-badge {
    position: absolute;
    top: 10px;
    right: 10px;
    padding: 4px 10px;
    background: linear-gradient(45deg, #ff7875, #ff5c57);
    border-radius: 12px;
    font-size: 12px;
    font-weight: bold;
    color: white;
  }
}

.live-info {
  padding: 15px;

  .live-title {
    font-size: 16px;
    font-weight: bold;
    color: #333;
    margin-bottom: 10px;
    cursor: pointer;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;

    &:hover {
      color: #1890ff;
    }
  }

  .live-time {
    display: flex;
    align-items: center;
    color: #666;
    font-size: 13px;
    margin-bottom: 15px;

    .time-icon {
      margin-right: 5px;
    }
  }

  .live-actions {
    display: flex;
    gap: 10px;

    .watch-btn {
      flex: 1;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      border-radius: 4px;

      &:disabled {
        background: #f5f5f5;
        color: #aaa;
        border-color: #ddd;
      }
    }

    .detail-btn {
      border-radius: 4px;
    }
  }
}

// 直播详情弹窗样式
.live-detail-content {
  .detail-item {
    display: flex;
    margin-bottom: 15px;
    align-items: flex-start;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .detail-label {
      font-weight: bold;
      min-width: 100px;
      color: #666;
      flex-shrink: 0;
    }

    .detail-value {
      flex: 1;
      word-break: break-all;
      color: #333;

      &.detail-link {
        color: #1890ff;
        cursor: pointer;
        transition: color 0.3s;

        &:hover {
          color: #40a9ff;
        }
      }

      &.vip-status {
        color: #ff7875;
      }

      &.normal-status {
        color: #52c41a;
      }

      &.status-active {
        color: #52c41a;
      }

      &.status-inactive {
        color: #bfbfbf;
      }
    }
  }

  .cover-image-container {
    flex: 1;
    margin-top: 5px;

    .cover-image {
      max-width: 200px;
      max-height: 150px;
      border-radius: 4px;
      border: 1px solid #e8e8e8;
      object-fit: cover;
    }
  }

  .detail-actions {
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #eee;
  }
}

// 直播间弹窗样式
.live-room-content {
  .video-container {
    width: 100%;
    background: #000;
  }

  .live-room-info {
    padding: 15px;
    border-bottom: 1px solid #f0f0f0;

    h3 {
      margin: 0 0 10px 0;
      font-size: 18px;
      color: #333;
    }

    .room-time,
    .room-vip {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      color: #666;

      .anticon {
        margin-right: 5px;
      }
    }

    .room-status {
      span {
        padding: 2px 8px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: bold;

        &.status-active {
          background: #52c41a;
          color: white;
        }

        &.status-inactive {
          background: #bfbfbf;
          color: white;
        }
      }
    }
  }
}

// 弹窗整体样式优化
/deep/ .ant-modal-body {
  padding: 0;
}

// 视频播放器样式
.video-js {
  width: 100% !important;
  height: 500px !important;
  background-color: #000;
}
</style>
