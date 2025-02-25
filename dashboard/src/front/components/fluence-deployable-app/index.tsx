import * as React from 'react';
import {connect} from 'react-redux';
import {withRouter} from "react-router";
import {DeployableApp, DeployableAppId, deployableApps, StorageType} from "../../../fluence/deployable";
import {deploy, deployUpload, showModal} from "../../actions";
import {Action} from "redux";
import {remove0x, toIpfsHash} from "../../../utils";
import FluenceId from '../fluence-id';
import {History} from "history";
import {ipfsDownloadUrl} from "../../../constants";
import { ReduxState } from '../../app';

interface State {
    loading: boolean,
}

export interface DeployUploadSate {
    uploaded: boolean
    uploading: boolean,
    data: object,
    storageHash: string,
    error?: any,
}

interface Props {
    id: DeployableAppId,
    deploy: (app: DeployableApp, appId: string, storageHash: string, history: History) => Promise<Action>,
    history: History;
    deployUpload: (form: FormData) => Promise<Action>,
    deployState: { state: string } | undefined,
    upload: DeployUploadSate,
    isMetamaskActive: boolean,
    showModal: typeof showModal,
    modal: any,
}

class FluenceDeployableApp extends React.Component<Props, State> {
    state: State = {
        loading: false,
    };

    uploadFormElement: HTMLInputElement;

    getDeployStateLabel(deployState: any): string {
        switch (deployState.state) {
            case 'prepare': {
                return 'preparing transaction...';
            }
            case 'trx': {
                return 'sending transaction...';
            }
            case 'enqueued': {
                return 'app is enqueued...';
            }
            case 'check_cluster': {
                return `${deployState.note}...`;
            }
            default: {
                return '';
            }
        }
    }

    async showModal(): Promise<boolean> {
        if (this.props.isMetamaskActive || this.props.modal.alreadyOpened) {
            return true;
        }

        return new Promise(resolve => {
            this.props.showModal({
                once: true,
                deployText: true,
                okCallback: () => resolve(true),
                cancelCallback: () => resolve(false),
            });
        });
    }

    startDeploy = async (e: React.MouseEvent<HTMLElement>, app: DeployableApp, appId: string) => {
        if (!await this.showModal()) {
            return;
        }

        this.setState({loading: true});
        this.props.deploy(app, appId, this.props.upload.storageHash, this.props.history)
            .catch(function (err) {
                console.error("error while deploying " + JSON.stringify(err));
            })
            .then(() => this.setState({loading: false}));
    };

    startUpload = async (e: React.MouseEvent<HTMLElement>, app: DeployableApp, appId: string) => {
        e.preventDefault();

        if (!await this.showModal()) {
            return;
        }

        if (!this.uploadFormElement || !this.uploadFormElement.files || this.uploadFormElement.files.length == 0) {
            return;
        }

        const form = new FormData();
        form.append('file', this.uploadFormElement.files[0]);

        this.props.deployUpload(form).then(() => {
            this.setState({loading: true});
            return this.props.deploy(app, appId, this.props.upload.storageHash, this.props.history);
        }).catch(function (err) {
            console.error("error while deploying " + JSON.stringify(err));
        }).then(() => this.setState({loading: false}));
    };

    renderStorageHashBlock(app: DeployableApp): React.ReactNode[] {
        let block = [
            <strong><i className="fa fa-bullseye margin-r-5"/>WebAssembly package</strong>
        ];

        if (app.selfUpload && this.props.upload.storageHash == '') {
            return [];
        } else if (app.storageType == StorageType.Ipfs) {
            let storageHash = app.selfUpload ? this.props.upload.storageHash : app.storageHash;
            block.push(
                <p className="text-muted"><FluenceId
                    isLink={true}
                    entityId={storageHash}
                    href={ipfsDownloadUrl + toIpfsHash(storageHash)}
                    target="_blank"
                    rel="noreferrer"
                    download/></p>
            );
        } else {
            block.push(
                <p className="text-muted"><FluenceId
                    entityId={app.storageHash}
                    isLink={true}
                    href={'https://swarm-gateways.net/bzz:/' + remove0x(app.storageHash) + '/' + app.name + '.wasm'}
                    target="_blank"/></p>
            );
        }

        return block;
    }

    renderRepoLinkBlock(app: DeployableApp): React.ReactNode[] {
        return [
            <strong><i className="fa fa-bullseye margin-r-5"/>GitHub repo</strong>,
            <p className="text-muted"><a
                href={app.repoLink}
                title={app.repoLink}
                target="_blank">{app.repoLink}</a></p>
        ]
    }

    renderUploadBlock(): React.ReactNode[] {
        return ([
            <strong><i className="fa fa-bullseye margin-r-5"/>Upload *.wasm file</strong>,
            <p><input type="file" ref={(ref: HTMLInputElement) => { this.uploadFormElement = ref; }} accept=".wasm"/></p>,
            <hr/>
        ]);
    }

    isDeployButtonDisabled(app: DeployableApp): boolean {
        if(app.selfUpload) {
            return !!(this.props.upload.uploading || this.props.upload.uploaded);
        } else {
            return this.state.loading;
        }
    }

    renderAppInfo(app: DeployableApp, appId: string): React.ReactNode {
        return (
            <div className="box-footer no-padding">
                <div className="box-body">
                    {app.selfUpload && this.renderUploadBlock()}

                    {this.renderStorageHashBlock(app)}

                    <strong><i className="fa fa-bullseye margin-r-5"/>Cluster Size</strong>
                    <p className="text-muted">{app.clusterSize} nodes</p>

                    {app.repoLink && this.renderRepoLinkBlock(app)}
                    <hr/>

                    <p>
                        <button
                            type="button"
                            onClick={e => app.selfUpload ? this.startUpload(e, app, appId) : this.startDeploy(e, app, appId)}
                            disabled={this.isDeployButtonDisabled(app)}
                            className="btn btn-block btn-success btn-lg">
                            Deploy app {!this.props.isMetamaskActive && '(demo mode)'} <i style={{display: (this.state.loading || this.props.upload.uploading) ? 'inline-block' : 'none'}}
                                          className="fa fa-refresh fa-spin"/>
                        </button>
                        {this.props.deployState && <span>Status: {this.getDeployStateLabel(this.props.deployState)}</span>}
                    </p>
                </div>
            </div>
        );
    }

    render(): React.ReactNode {
        const app = deployableApps[this.props.id];

        return (
            <div className="box box-widget widget-user-2">
                <div className="widget-user-header bg-fluence-blue-gradient">
                    <div className="widget-user-image">
                        <span className="entity-info-box-icon entity-info-box-icon-thin"><i
                            className={app ? 'ion ion-ios-gear-outline' : 'fa fa-refresh fa-spin'}></i></span>
                    </div>
                    <h3 className="widget-user-username">{app.name}</h3>
                </div>
                {app && this.renderAppInfo(app, this.props.id)}
            </div>
        );
    }
}

const mapStateToProps = (state: ReduxState) => ({
    upload: state.deploy.upload,
    deployState: state.deploy.deployState,
    modal: state.modal,
    isMetamaskActive: state.ethereumConnection.isMetamaskProviderActive,
});

const mapDispatchToProps = {
    deploy,
    deployUpload,
    showModal,
};

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(FluenceDeployableApp));
