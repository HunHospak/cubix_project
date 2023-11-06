import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http'
import { Observable } from 'rxjs';
import { Flight } from '../model/Flight';

@Injectable({
  providedIn: 'root'
})
export class FlightService {

  private productsUrl = 'api/flights/search';

  private httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(
    private http: HttpClient
  ) { }

  getFlights(): Observable<Flight[]> {
    return this.http.get<Flight[]>(this.productsUrl);
  } 
}
