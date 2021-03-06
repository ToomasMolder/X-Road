# X-Road installation instructions

## 1. Install Ansible

Install Ansible by following instructions at http://docs.ansible.com/ansible/intro_installation.html

## 2. Configuration options

The servers to be installed are written in the Ansible inventory (hosts-file). 
In the provided example command they are in `hosts/example_xroad_hosts.txt`. 
Host names in the file must be correct fully qualified host names because they are used 
in X-Road certificate generation and setting the hostname of the servers when installing. 
Do not use IP addresses.

You determine which servers are initialized by filling in the groups 
`cs-servers`, `ss-servers`, `cp-servers` and `ca-servers`. If you have no use for such servers,
you can leave that group empty.

Parameters such as the admin username and password, and CA server's distinguished name values, 
can be configured using files in `group_vars` directory. 
In the provided example the configuration file is `group_vars/example.yml`. 
The link between the inventory file and the variable definition file is based on (in this example) a group name. 
`example_xroad_hosts.txt` defines a host group
```
[example:children]
 cs-servers
 ss-servers
 cp-servers
 ca-server
```
containing all the hosts in the inventory. 
Ansible searches for variables from `group_vars/example.yml` when initializing hosts from "example" group.

Username and password are also defined in the default variables of the `xroad-base` role. This definition will be used
if no group vars or other higher precedence configuration for the same variables have been assigned.
 
## 3. Install X-Road using Ansible

```
ansible-playbook -i hosts/example_xroad_hosts.txt xroad_init.yml
```

This installs or updates **all X-Road related packages** to latest versions **from package repositories**. 

For the X-Road's own packages, the default repository is either
`http://www.nic.funet.fi/pub/csc/x-road/client/ubuntu-prod-current` 
or 
`http://www.nic.funet.fi/pub/csc/x-road/client/rhel7-prod-current`.

The used repository can be configured in `vars_files/remote_repo.yml`.

The Ansible playbooks currently only support the installation of X-Road according to the Finnish configuration. 

## 4. Development using LXD

On Linux it is possible to use the LXD container hypervisor for fast testing and development.

First, make sure that you have LXD installed and configured as explained on https://linuxcontainers.org/lxd/getting-started-cli/. For example, on Ubuntu:

```
sudo apt-get install lxd

# Log in to the new group lxd (or logout/login)
newgrp lxd

# Configure lxd
sudo lxd init (use bridge network configuration and turn on nat)
```
Study the provided LXD-specific host file `hosts/lxd_hosts.txt` and modify the inventory or create your own host file as required.
The playbook initializes LXD-containers according to the hosts defined in the inventory. The host for the LXD-containers themselves is
defined with the group `lxd-servers`, normally localhost.

Install packages to local LXD containers from public repository with:

```
ansible-playbook  -i hosts/lxd_hosts.txt xroad_init.yml
```

Compile your own X-Road, build packages and install X-Road from your own packages using the playbook `xroad_dev.yml`.

First make sure that docker and docker-py are installed. Docker is used for building packages on localhost.

```
ansible-playbook  -i hosts/lxd_hosts.txt xroad_dev.yml
```

This updates **all X-Road related packages to their latest versions** using **locally built X-Road packages** (as long as `compile-servers` group in ansible inventory = `localhost`). 
Package version names are formed using current git commit timestamp and hash. This means that

* if you only make local changes without performing a git commit, package names and version numbers are
not changed - and executing xroad_dev playbook will keep the old packages intact 
* if you perform a git commit (of any kind), all local modifications will be packaged and deployed 
using the new version number - and all local modifications (whether they were included in the commit or not)
will be deployed

In short, to see changes, **a git commit must be done**.
 
### Partial compilation and deployment

For fast development, you can compile and install modules separately. The ansible playbook `xroad_dev_partial.yml` offers this. For example, if you make a change to a Java or Ruby file under proxy-ui, use the following command to compile the war and deploy it to the security servers.
```
ansible-playbook  -i hosts/lxd_hosts.txt   xroad_dev_partial.yml   -e selected_modules=proxy-ui
```

It is also possible to compile and install several modules (jars or wars). The following command compiles and installs common-util and signer jars and proxy-ui war to the correct servers.
```
ansible-playbook  -i hosts/lxd_hosts.txt   xroad_dev_partial.yml   -e selected_modules=common-util,proxy-ui,signer
```

This updates the **selected modules (jars or wars)** to ones compiled locally. 
**No git commits are needed** to see changes.

The modules are listed in dicts `common_modules.yml`, `cs_modules.yml`, `cp_modules.yml` and `ss_modules.yml`.

Note that the playbook `xroad_dev_partial.yml` only copies jars and wars to the servers in correct locations. For the full installation, use `xroad_dev.yml`.

## 5. Test CA, TSA, and OCSP

More information about these [here.](TESTCA.md)
