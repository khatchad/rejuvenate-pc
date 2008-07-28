select 
	ssh.advice_key as advice,
	ssh.version,
	ssh.shadow_key as suggestion, 
	max(ssh.confidence) as confidence,
	(select count(ssh3.*)
	from (
		select ssh2.shadow_key, 
			max(ssh2.confidence) as max_conf2
		from suggested_shadows ssh2
		where ssh2.advice_key = ssh.advice_key
		and ssh2.version = ssh.version
		group by ssh2.shadow_key
		having max(ssh2.confidence) > max(ssh.confidence)) ssh3) as above,
	(select count(ssh3.*)
	from (
		select ssh2.shadow_key, 
			max(ssh2.confidence) as max_conf2
		from suggested_shadows ssh2
		where ssh2.advice_key = ssh.advice_key
		and ssh2.version = ssh.version
		group by ssh2.shadow_key
		having max(ssh2.confidence) < max(ssh.confidence)) ssh3) as below
from suggested_shadows ssh
where ssh.shadow_key in 	
	(select ash.shadow_key
	from advises_shadow ash
	where ash.advice_key = ssh.advice_key
	and ash.version = ssh.version)
group by ssh.shadow_key
