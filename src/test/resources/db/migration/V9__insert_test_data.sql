
insert into Appointments(id, startTimestamp, endTimestamp)
values
  (gen_random_uuid(), '2020-06-14 00:00:00', '2020-12-31 23:59:59'),
  (gen_random_uuid(), '2020-06-14 15:00:00', '2020-06-14 18:30:00'),
  (gen_random_uuid(), '2020-06-15 00:00:00', '2020-06-15 11:00:00'),
  (gen_random_uuid(), '2020-06-15 16:00:00', '2020-12-31 23:59:59');

insert into AppointmentsUsers select a.id, u.id from Appointments a, Users u;
