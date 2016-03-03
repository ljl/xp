module api.security {

    export class UserStore extends api.item.BaseItem {
        private displayName: string;
        private key: UserStoreKey;
        private permissions: api.security.acl.UserStoreAccessControlList;

        constructor(builder: UserStoreBuilder) {
            super(builder);
            this.displayName = builder.displayName;
            this.key = builder.key;
            this.permissions = builder.permissions || new api.security.acl.UserStoreAccessControlList();
        }

        getDisplayName(): string {
            return this.displayName;
        }

        getKey(): UserStoreKey {
            return this.key;
        }

        getPermissions(): api.security.acl.UserStoreAccessControlList {
            return this.permissions;
        }

        static checkOnDeletable(key: UserStoreKey): wemQ.Promise<boolean> {
            return !!key ? UserStore.create().setKey(key.toString()).build().checkIsDeletable() : null;
        }

        private checkIsDeletable(): wemQ.Promise<boolean> {
            var deferred = wemQ.defer<boolean>();
            new GetPrincipalsByUserStoreRequest(this.key,
                [PrincipalType.USER, PrincipalType.GROUP]).
                sendAndParse().then((principals: Principal[]) => {
                    if (principals.length > 0) {
                        deferred.resolve(false);
                    } else {
                        deferred.resolve(true);
                    }
                }).catch((reason: any) => {
                    api.DefaultErrorHandler.handle(reason);
                    deferred.resolve(false);
                }).done();
            ;
            return deferred.promise;
        }

        equals(o: api.Equitable): boolean {
            if (!api.ObjectHelper.iFrameSafeInstanceOf(o, UserStore)) {
                return false;
            }

            var other = <UserStore> o;

            return this.key.equals(other.key) &&
                   this.displayName === other.displayName &&
                   this.permissions.equals(other.permissions)
        }

        clone(): UserStore {
            return new UserStoreBuilder(this).
                build();
        }

        static create(): UserStoreBuilder {
            return new UserStoreBuilder();
        }

        static fromJson(json: api.security.UserStoreJson): UserStore {
            return new UserStoreBuilder().fromJson(json).build();
        }
    }

    export class UserStoreBuilder extends api.item.BaseItemBuilder {
        displayName: string;
        key: UserStoreKey;
        permissions: api.security.acl.UserStoreAccessControlList;

        constructor(source?: UserStore) {
            super(source);

            if (source) {
                this.setDisplayName(source.getDisplayName());
                this.setKey(source.getKey().toString());
                this.setPermissions(source.getPermissions().clone());
            }
        }

        fromJson(json: api.security.UserStoreJson): UserStoreBuilder {
            super.fromBaseItemJson(json, 'key');
            this.key = new UserStoreKey(json.key);
            this.displayName = json.displayName;
            this.permissions = json.permissions ? api.security.acl.UserStoreAccessControlList.fromJson(json.permissions) : null;
            return this;
        }

        setKey(key: string): UserStoreBuilder {
            this.key = new UserStoreKey(key);
            return this;
        }

        setDisplayName(displayName: string): UserStoreBuilder {
            this.displayName = displayName;
            return this;
        }

        setPermissions(permissions: api.security.acl.UserStoreAccessControlList): UserStoreBuilder {
            this.permissions = permissions;
            return this;
        }

        build(): UserStore {
            return new UserStore(this);
        }
    }
}