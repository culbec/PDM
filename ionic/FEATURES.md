# Game Stop - Game rental app

`Game Stop` is a _game rental_ app that is supposed to be useful for users that
want to rental games, not own them.

## Features of the app

This app will allow the users to see available games provided by the store. (`TODO: having some users?`)

Each user can rent a game, considering it is not rented by someone else, and give it back when he decides to. (`TODO: database containing this kind of relation?`)

A _special kind of user_ (`administrator`), may add new items, by completing a form describing the new item to be added.

## Entities

1. Game

   - each game will have the following fields:
     - an _**id**_ for identification purposes;
     - a _**title**_;
     - a _**release date**_;
     - a _**rental price**_ - the users will have to pay through the app to rent a game (very very distant future feature);
     - a _**rental status**_ - either `RENTED` or `NOT RENTED`;
     - a _**user reference**_ - when the individual rental by each user feature will be actually implemented;
   - each _game_ will be considered an `item` in terms of a resource provided by the app server;

2. User

   - pretty self-explanatory, this will be the client that will actually use the app;
   - TODO: still a work in progress, for the moment the app will be available for a mock client to use, having limited resources and no memory whatsoever;

## App Contents

The app will contain a `master-detail interface`, presented as such:

- the `master` part will contain a list of _items_ (`games`), useful for the client to check which options does he have for game rental;
- the `detail` part will contain individual information about the _items_ (`games`), presented in a uniform way to the client so that it is pretty clear and useful for him:
  - information about the _item_ (`game`), in such way that a nice presentation is provided;
  - a `RENT` option, giving that the client actually wants to rent the game;

## Server

The server is written in the `Go` programming language, being a `REST server`, keeping track of the app's features and managing the app's backend logic.
