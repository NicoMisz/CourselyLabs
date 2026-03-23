<template>
  <div class="lesson-video-player">
    <video ref="videoEl" class="video-js vjs-big-play-centered vjs-fluid" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import videojs from 'video.js'
import type Player from 'video.js/dist/types/player'
import 'video.js/dist/video-js.css'

const props = defineProps<{
  src: string
}>()

const emit = defineEmits<{
  ended: []
  timeUpdate: [currentTime: number, duration: number]
}>()

const videoEl = ref<HTMLVideoElement>()
let player: Player | null = null

const savedSpeed = parseFloat(localStorage.getItem('cl-video-speed') || '1')

function initPlayer() {
  if (!videoEl.value) return

  player = videojs(videoEl.value, {
    controls: true,
    autoplay: false,
    preload: 'auto',
    playbackRates: [0.5, 0.75, 1, 1.25, 1.5, 1.75, 2],
    controlBar: {
      pictureInPictureToggle: true,
    },
    sources: [{ src: props.src, type: detectType(props.src) }],
  })

  player.playbackRate(savedSpeed)

  player.on('ratechange', () => {
    const rate = player?.playbackRate()
    if (rate) localStorage.setItem('cl-video-speed', String(rate))
  })

  player.on('ended', () => emit('ended'))

  player.on('timeupdate', () => {
    const ct = player?.currentTime() ?? 0
    const dur = player?.duration() ?? 0
    emit('timeUpdate', ct, dur)
  })
}

function getCurrentTime(): number {
  return player?.currentTime() ?? 0
}

function seekTo(seconds: number) {
  player?.currentTime(seconds)
}

defineExpose({ getCurrentTime, seekTo })

function detectType(url: string): string {
  if (url.endsWith('.m3u8')) return 'application/x-mpegURL'
  if (url.endsWith('.mp4')) return 'video/mp4'
  if (url.endsWith('.webm')) return 'video/webm'
  return 'video/mp4'
}

watch(() => props.src, (newSrc) => {
  if (player) {
    player.src({ src: newSrc, type: detectType(newSrc) })
  }
})

onMounted(initPlayer)

onBeforeUnmount(() => {
  if (player) {
    player.dispose()
    player = null
  }
})
</script>

<style scoped>
.lesson-video-player {
  width: 100%;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}
</style>
