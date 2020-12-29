import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';
import { Observable, of, pipe, OperatorFunction } from 'rxjs';
import { Creator } from '@app/_models';
import { flatMap, filter, toArray } from 'rxjs/operators';

const fake_data: Creator[] = [
  {
    id: '1',
    name: 'Mr. Artsypants',
    summary: 'i am an artist',
    links: {
      web: "http://google.com",
      preferred: "web"
    },
    images: [{
      creator_id: '1',
      url: "https://homepages.cae.wisc.edu/~ece533/images/baboon.png",
      alt: "This is a picture of a beautiful babboon.",
      title: "babboon at rest"
    }]
  },
  {
    id: '2',
    name: 'Mx. Artsypants',
    summary: 'i am a better artist',
    links: {
      insta: "http://instagram.com",
      web: "http://google.com",
      preferred: "insta"
    },
    images: [{
      creator_id: '2',
      url: "https://homepages.cae.wisc.edu/~ece533/images/airplane.png",
      alt: "This is a picture of a more beautiful airplane.",
      title: "airplane in motion"
    }]
  },
  {
    id: '3',
    name: 'Mr. Artsypants 3',
    summary: 'i am an artist',
    images: [{
      creator_id: '3',
      url: "https://homepages.cae.wisc.edu/~ece533/images/baboon.png",
      alt: "This is a picture of a beautiful babboon.",
      title: "babboon at rest"
    }]
  },
  {
    id: '4',
    name: 'Mr. Artsypants 4',
    summary: 'i am an artist',
    images: [{
      creator_id: '4',
      url: "https://homepages.cae.wisc.edu/~ece533/images/baboon.png",
      alt: "This is a picture of a beautiful babboon.",
      title: "babboon at rest"
    }]
  },
  {
    id: '5',
    name: 'Mr. Artsypants 5',
    summary: 'i am an artist',
    images: [{
      creator_id: '5',
      url: "https://homepages.cae.wisc.edu/~ece533/images/baboon.png",
      alt: "This is a picture of a beautiful babboon.",
      title: "babboon at rest"
    }]
  },
  {
    id: '6',
    name: 'Mr. Artsypants 6',
    summary: 'i am an artist',
    images: [{
      creator_id: '6',
      url: "https://homepages.cae.wisc.edu/~ece533/images/baboon.png",
      alt: "This is a picture of a beautiful babboon.",
      title: "babboon at rest"
    }]
  },
  {
    id: '7',
    name: 'Mr. Artsypants 7 ',
    summary: 'i am an artist',
    images: [{
      creator_id: '7',
      url: "https://homepages.cae.wisc.edu/~ece533/images/baboon.png",
      alt: "This is a picture of a beautiful babboon.",
      title: "babboon at rest"
    }]
  },
]

@Injectable({
  providedIn: 'root'
})
export class CreatorService {

  constructor( private http: HttpClient) { }

  make_id_filter(ids?: string[]) : OperatorFunction<Creator[], Creator[]> {
    if (ids) {
      return pipe(
        flatMap(x => of(...x)),
        filter((c: Creator) => ids.includes(c.id)),
        toArray()
      )
    }
    return pipe()
  }

  get_artists(ids?: string[]) : Observable<Creator[]>{
    return of(fake_data).pipe(
      this.make_id_filter(ids)
    );
    /*return this.http.get<Creator[]>(`${environment.backend}/artists`).pipe(
      this.make_id_filter(ids)
    );*/
  }

  get_dealers(ids?: string[]) : Observable<Creator[]>{
    return this.http.get<Creator[]>(`${environment.backend}/dealers`).pipe(
      this.make_id_filter(ids)
    );
  }
}
