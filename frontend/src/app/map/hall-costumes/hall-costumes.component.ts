import { Component, OnInit, ViewChild } from '@angular/core';
import { NgbCarousel, NgbSlideEvent } from '@ng-bootstrap/ng-bootstrap';

interface Image {
  src: string;
  caption: string;
};

function moduloDecrement(value: number, divisor: number): number {
  return (value + divisor - 1) % divisor;
}

function moduloIncrement(value: number, divisor: number): number {
  return (value + 1) % divisor;
}

@Component({
  selector: 'app-hall-costumes',
  templateUrl: './hall-costumes.component.html',
  styleUrls: ['./hall-costumes.component.scss']

})
export class HallCostumesComponent implements OnInit {
  images: Image[] = [];
  currentImage: number = 0;

  imageBuffer: Image[] = [];
  currentImageBuffer: number[] = [];
  currentFrame: number = 0;

  paused: boolean = false;

  @ViewChild(NgbCarousel) carousel!: NgbCarousel;

  constructor() { }

  ngOnInit(): void {
    this.images = [{src: 'https://coolbackgrounds.io/images/backgrounds/white/pure-white-background-85a2a7fd.jpg', caption: 'This Image Is Totally White'}].concat([1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20].map(
    (n) => ({src: `https://picsum.photos/id/${n}/900/500`, caption: `Image number ${n}`})));
    this.updateImageBufferSize();
    this.forceUpdateImageBuffer();
  }

  play(): void {
    this.carousel.next();
    this.carousel.cycle();
    this.paused = false;
  }

  pause(): void {
    this.carousel.pause();
    this.paused = true;
  }

  handleSlidEvent(event: NgbSlideEvent) {
    let backward = false;
    if (event.direction !== 'right') {
      // Go forward
      this.currentFrame = moduloIncrement(this.currentFrame, this.imageBuffer.length);
      this.currentImage = moduloIncrement(this.currentImage, this.images.length);
    } else {
      // Go backward
      this.currentFrame = moduloDecrement(this.currentFrame, this.imageBuffer.length);
      this.currentImage = moduloDecrement(this.currentImage, this.images.length);
    }
    this.updateImageBuffer();
    this.paused = event.paused;
  }

  updateImageBufferSize(): void {
    this.imageBuffer.length = Math.min(this.images.length, 3);
    if (this.currentImage > this.images.length) {
      this.currentImage = 0;
    }
    if (this.currentFrame > this.imageBuffer.length) {
      this.currentFrame = 0;
    }
  }

  updateImageBuffer(): void {
    if (this.imageBuffer.length > 0) {
      this.updateImage(this.currentFrame, this.currentImage);
    }
    if (this.imageBuffer.length > 1) {
      this.updateImage(moduloIncrement(this.currentFrame, this.imageBuffer.length),
                       moduloIncrement(this.currentImage, this.images.length))
    }
    if (this.imageBuffer.length > 2) {
      this.updateImage(moduloDecrement(this.currentFrame, this.imageBuffer.length),
                       moduloDecrement(this.currentImage, this.images.length))
    }
  }

  updateImage(frame: number, image: number): void {
    if (this.currentImageBuffer[frame] !== image) {
      this.forceUpdateImage(frame, image);
    }
  }

  forceUpdateImageBuffer(): void {
    if (this.imageBuffer.length > 0) {
      this.forceUpdateImage(this.currentFrame, this.currentImage);
    }
    if (this.imageBuffer.length > 1) {
      this.forceUpdateImage(moduloIncrement(this.currentFrame, this.imageBuffer.length),
                            moduloIncrement(this.currentImage, this.images.length))
    }
    if (this.imageBuffer.length > 2) {
      this.forceUpdateImage(moduloDecrement(this.currentFrame, this.imageBuffer.length),
                            moduloDecrement(this.currentImage, this.images.length))
    }
  }

  forceUpdateImage(frame: number, image: number): void {
    this.currentImageBuffer[frame] = image;
    this.imageBuffer[frame] = this.images[image];
  }
}
