set @organization_id = 1;

-- Ownership corrections
insert into ownership_correction(organization_id, correction, object_id, organization_code) values
(@organization_id,UPPER('�i������ ���'),1,'1'),
(@organization_id,UPPER('������������'),2,'1'),
(@organization_id,UPPER('��������'),5,'5'),
(@organization_id,UPPER('�������������'),6,'6'),
(@organization_id,UPPER('����������'),4,'4'),
(@organization_id,UPPER('�i�����'),3,'3');
