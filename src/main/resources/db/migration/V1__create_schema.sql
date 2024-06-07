
create table Appointments (
  id uuid not null,
  startTimestamp timestamp not null,
  endTimestamp timestamp not null,

  check(endTimestamp >= startTimestamp),

  primary key (id)
);

create index AppointmentsStartEnd on Appointments(startTimestamp, endTimestamp);

create table Users (
  id uuid not null,
  name varchar(128) not null,

  check(name != ''),

  primary key (id)
);

create table AppointmentsUsers (
  appointmentId uuid not null references Appointments(id),
  userId uuid not null references Users(id),

  primary key (appointmentId, userId)
);
