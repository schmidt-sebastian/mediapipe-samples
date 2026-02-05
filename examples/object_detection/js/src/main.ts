import './style.css';
import {
  ObjectDetector,
  FilesetResolver,
  ObjectDetectorResult
} from '@mediapipe/tasks-vision';

const demosSection = document.getElementById('demos') as HTMLElement;
let objectDetector: ObjectDetector | undefined;
let runningMode: 'IMAGE' | 'VIDEO' = 'IMAGE';

// Initialize the object detector
const initializeObjectDetector = async () => {
  const vision = await FilesetResolver.forVisionTasks('/wasm');
  
  objectDetector = await ObjectDetector.createFromOptions(vision, {
    baseOptions: {
      modelAssetPath: `https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite0/float32/1/efficientdet_lite0.tflite`,
      delegate: 'GPU',
    },
    scoreThreshold: 0.5,
    runningMode: runningMode,
  });
  
  demosSection.classList.remove('invisible');
};
initializeObjectDetector();

const video = document.getElementById('webcam') as HTMLVideoElement;
const canvasElement = document.getElementById('output_canvas') as HTMLCanvasElement;
const canvasCtx = canvasElement.getContext('2d')!;
const enableWebcamButton = document.getElementById('webcamButton') as HTMLButtonElement;

// Check if webcam access is supported
const hasGetUserMedia = () => !!navigator.mediaDevices?.getUserMedia;

if (hasGetUserMedia()) {
  enableWebcamButton.addEventListener('click', enableCam);
} else {
  console.warn('getUserMedia() is not supported by your browser');
}

async function enableCam() {
  if (!objectDetector) {
    console.log('Wait! objectDetector not loaded yet.');
    return;
  }

  // Toggle button text
  if (video.paused) {
    enableWebcamButton.innerText = 'Disable Webcam';
    const constraints = {
      video: true,
    };

    try {
      const stream = await navigator.mediaDevices.getUserMedia(constraints);
      video.srcObject = stream;
      video.addEventListener('loadeddata', predictWebcam);
    } catch (err) {
      console.error(err);
    }
  } else {
    // Stop the camera
    const stream = video.srcObject as MediaStream;
    const tracks = stream.getTracks();
    tracks.forEach((track) => track.stop());
    video.srcObject = null;
    enableWebcamButton.innerText = 'Enable Webcam';
  }
}

let lastVideoTime = -1;
async function predictWebcam() {
  if (!objectDetector) return;
  // If video is not active, stop loop
  if (!video.srcObject) return;

  // Change running mode to VIDEO for stream
  if (runningMode === 'IMAGE') {
    runningMode = 'VIDEO';
    await objectDetector.setOptions({ runningMode: 'VIDEO' });
  }

  let startTimeMs = performance.now();

  // Detect objects if frame is new
  if (video.currentTime !== lastVideoTime) {
    lastVideoTime = video.currentTime;
    const detections = objectDetector.detectForVideo(video, startTimeMs);
    displayVideoDetections(detections);
  }

  window.requestAnimationFrame(predictWebcam);
}

function displayVideoDetections(result: ObjectDetectorResult) {
  // Match canvas size to video size
  canvasElement.width = video.videoWidth;
  canvasElement.height = video.videoHeight;

  canvasCtx.save();
  canvasCtx.clearRect(0, 0, canvasElement.width, canvasElement.height);

  if (result.detections) {
    for (let detection of result.detections) {
      canvasCtx.beginPath();
      canvasCtx.lineWidth = 4;
      canvasCtx.strokeStyle = '#007f8b'; 

      const { originX, originY, width, height } = detection.boundingBox!;

      // Use mirroredX instead of originX for drawing
      const mirroredX = video.videoWidth - width - originX;
      canvasCtx.strokeRect(mirroredX, originY, width, height);

      // Draw label background
      canvasCtx.fillStyle = '#007f8b';
      canvasCtx.font = '16px sans-serif';
      
      const category = detection.categories[0];
      const score = category.score ? Math.round(category.score * 100) : 0;
      const labelText = `${category.categoryName} - ${score}%`;

      const textWidth = canvasCtx.measureText(labelText).width;
      canvasCtx.fillRect(mirroredX, originY, textWidth + 10, 25);

      // Draw label text
      canvasCtx.fillStyle = '#ffffff';
      canvasCtx.fillText(labelText, mirroredX + 5, originY + 18);
      // --- FIX END ---
    }
  }
  canvasCtx.restore();
}