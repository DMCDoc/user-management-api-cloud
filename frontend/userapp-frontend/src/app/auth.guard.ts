// src/app/auth.guard.ts
import { Injectable } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const AuthGuard: CanActivateFn = () => {
  const token = localStorage.getItem('token');
  if (token) {
    return true;
  } else {
    window.alert('Veuillez vous connecter.');
    return false;
  }
};
